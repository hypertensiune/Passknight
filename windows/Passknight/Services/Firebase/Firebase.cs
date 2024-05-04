﻿using Passknight.Core;
using Passknight.Models;
using Passknight.Models.Items;
using Passknight.Stores;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Services.Firebase
{
    /// <summary>
    /// Provides a class for accessing FIrebase features.
    /// Main wrapper around firebase REST Api.
    /// </summary>
    class Firebase
    {
        private Auth authentification;
        private Firestore firestore;

        private FirebaseStore firebaseStore;

        private List<string> vaults = new List<string>();
        
        public Firebase(string API_KEY)
        {
            authentification = new Auth(API_KEY);
            firestore = new Firestore();
            firebaseStore = new FirebaseStore();
        }

        private static string Email(string vault) => $"{vault}@passknight.vault";

        public async Task<List<string>> GetVaultNames()
        {
            string res = await firestore.GetDoc("ids");
            vaults = JSONConverter.VaultNames(res);

            return vaults;
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
            string res = await firestore.GetDoc(firebaseStore.CurrentUnlockedVaultID, authentification.ID_TOKEN);
            VaultContent content = JSONConverter.VaultContent(res);

            return new Vault(firebaseStore.CurrentUnlockedVaultName, content);
        }

        public async Task<bool> CreateNewVault(string vault, string password)
        {
            var (response, ID) = await authentification.CreateUserWithEmailAndPassword(Email(vault), password);
            if(response)
            {
                firebaseStore.CurrentUnlockedVaultID = ID!;
                firebaseStore.CurrentUnlockedVaultName = vault;

                var body = """
                {
                    "fields": {
                        "salt": {
                            "stringValue": "salt"
                        },
                        "passwords": {
                            "arrayValue": {}
                        },
                        "notes": {
                            "arrayValue": {}
                        },
                        "history": {
                            "arrayValue": {}
                        },
                    }
                }
                """;

                var body2 = $$"""
                {
                    "fields": {
                        "{{vault}}": {
                            "stringValue": "{{ID!}}"
                        }
                    }
                }
                """;

                await firestore.SetDoc(ID!, body, authentification.ID_TOKEN);
                await firestore.UpdateDoc("ids", vault, body2, authentification.ID_TOKEN);
            }

            return response;
        }

        /// <summary>
        /// Used to update the passwords, notes and history fields in the currently unlocked vault. <br/>
        /// <typeparamref name="T"/> is <see cref="string"/> => history <br/>
        /// <typeparamref name="T"/> is <see cref="NoteItem"/> => notes <br/>
        /// <typeparamref name="T"/> is <see cref="PasswordItem"/> => passwords <br/>
        /// </summary>
        public async Task<bool> UpdateFieldInVault<T>(List<T> items)
        {
            if (typeof(T) == typeof(PasswordItem))
            {
                string body = JSONConverter.PasswordItems(items as List<PasswordItem>);
                var res = await firestore.UpdateDoc(firebaseStore.CurrentUnlockedVaultID, "passwords", body, authentification.ID_TOKEN);
            }
            else if(typeof(T) == typeof(NoteItem))
            {
                string body = JSONConverter.NoteItems(items as List<NoteItem>);
                var res = await firestore.UpdateDoc(firebaseStore.CurrentUnlockedVaultID, "notes", body, authentification.ID_TOKEN);
            }
            else if(typeof(T) == typeof(string))
            {

            }

            return true;
        }
    }
}
