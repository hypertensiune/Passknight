using Passknight.Models;
using Passknight.Models.Items;
using Passknight.Stores;

namespace Passknight.Services.Firebase
{
    /// <summary>
    /// Provides a class for accessing Firebase features.
    /// Main wrapper around firebase REST Api. <br/>
    /// Implements the <see cref="IDatabase"/> interface.
    /// </summary>
    class Firebase : IDatabase
    {
        private Auth authentification;
        private Firestore firestore;

        private FirebaseStore firebaseStore;
        
        public Firebase(string API_KEY)
        {
            authentification = new Auth(API_KEY);
            firestore = new Firestore();
            firebaseStore = new FirebaseStore();
        }

        private static string Email(string vault) => $"{vault}@passknight.vault";

        public async Task<List<string>> GetVaultNames()
        {
            string res = await firestore.GetDoc("vaults", "ids");
            return JSONConverter.VaultNames(res);
        }

        public async Task<bool> UnlockVault(string vault, string password)
        {
            var (response, ID) = await authentification.SignIn(Email(vault), password);

            if(response)
            {
                firebaseStore.CurrentUnlockedVaultID = ID!;
                firebaseStore.CurrentUnlockedVaultName = vault;
            }

            return response;
        }

        public void LockVault()
        {
            authentification.SignOut();
            firebaseStore.Clear();
        }

        public async Task<Vault> GetVault()
        {
            string passwords = await firestore.GetDoc(firebaseStore.CurrentUnlockedVaultID, "passwords", authentification.ID_TOKEN);
            string notes = await firestore.GetDoc(firebaseStore.CurrentUnlockedVaultID, "notes", authentification.ID_TOKEN);
            string history = await firestore.GetDoc(firebaseStore.CurrentUnlockedVaultID, "history", authentification.ID_TOKEN);
            string psk = await firestore.GetDoc(firebaseStore.CurrentUnlockedVaultID, "psk", authentification.ID_TOKEN);

            VaultContent content = JSONConverter.CreateVaultContent(passwords, notes, history, psk);

            return new Vault(firebaseStore.CurrentUnlockedVaultName, content);
        }

        public async Task<bool> CreateNewVault(string vault, string password, string psk)
        {
            var (response, ID) = await authentification.CreateUserWithEmailAndPassword(Email(vault), password);
            if(response)
            {
                firebaseStore.CurrentUnlockedVaultID = ID!;
                firebaseStore.CurrentUnlockedVaultName = vault;

                var bodyids = $$""" { "fields": { "{{vault}}": { "stringValue": "{{ID!}}" } } } """;

                var bodypsk = $$""" { "psk": { "stringValue": "{{psk}}" } } """;

                await firestore.SetDoc(ID!, "passwords", "", authentification.ID_TOKEN);
                await firestore.SetDoc(ID!, "notes", "", authentification.ID_TOKEN);
                await firestore.SetDoc(ID!, "psk", bodypsk, authentification.ID_TOKEN);

                await firestore.UpdateDoc("vaults", "ids", vault, bodyids, authentification.ID_TOKEN);
            }

            return response;
        }

        public async Task<bool> DeleteVault()
        {
            await firestore.UpdateDoc("vaults", "ids", firebaseStore.CurrentUnlockedVaultName, "", authentification.ID_TOKEN);
            await firestore.DeleteDoc(firebaseStore.CurrentUnlockedVaultID, "passwords", authentification.ID_TOKEN);

            return true;
        }

        public async Task<bool> AddItemInVault<T>(T item) where T : notnull
        {
            if (typeof(T) == typeof(PasswordItem))
            {
                string body = JSONConverter.EncodePasswordItem((item as PasswordItem)!);
                await firestore.UpdateDoc(firebaseStore.CurrentUnlockedVaultID, "passwords", (item as PasswordItem)!.Name, body, authentification.ID_TOKEN);
            }
            else
            {
                string body = JSONConverter.EncodeNoteItem((item as NoteItem)!);
                await firestore.UpdateDoc(firebaseStore.CurrentUnlockedVaultID, "notes", (item as NoteItem)!.Name, body, authentification.ID_TOKEN);
            }

            return true;
        }

        public async Task<bool> EditItemInVault<T>(T oldItem, T newItem) where T : notnull
        {
            // If the names of the items are different, add the new item and then delete
            // the old one. Otherwise simply update the existing item.

            await AddItemInVault(newItem);

            if(typeof(T) == typeof(PasswordItem) && (oldItem as PasswordItem)!.Name != (newItem as PasswordItem)!.Name)
            {
                await DeleteItemFromVault(oldItem);
            }
            
            if(typeof(T) == typeof(NoteItem) && (oldItem as NoteItem)!.Name != (newItem as NoteItem)!.Name)
            {
                await DeleteItemFromVault(oldItem);
            }

            return true;
        }

        public async Task<bool> DeleteItemFromVault<T>(T item) where T : notnull
        {
            // To delete a field use an empty input document
            // https://firebase.google.com/docs/firestore/reference/rest/v1beta1/projects.databases.documents/patch
            if(typeof(T) == typeof(PasswordItem))
            {
                await firestore.UpdateDoc(firebaseStore.CurrentUnlockedVaultID, "passwords", (item as PasswordItem)!.Name, "", authentification.ID_TOKEN);
            } else
            {
                await firestore.UpdateDoc(firebaseStore.CurrentUnlockedVaultID, "notes", (item as NoteItem)!.Name, "", authentification.ID_TOKEN);
            }

            return true;
        }

        public async Task<bool> SetGeneratorHistory(List<string> history)
        {
            var body = JSONConverter.EncodeHistory(history);
            await firestore.UpdateDoc(firebaseStore.CurrentUnlockedVaultID, "history", "history", body, authentification.ID_TOKEN);
            return true;
        }
    }
}
