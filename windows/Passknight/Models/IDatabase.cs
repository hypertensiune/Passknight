using Passknight.Models.Items;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Models
{
    /// <summary>
    /// Interface for managing a database. Provides all the methods to operate on a databse of vaults.
    /// </summary>
    interface IDatabase
    {
        /// <summary>
        /// Get a list of all the available vaults.
        /// </summary>
        public Task<List<string>> GetVaultNames();

        /// <summary>
        /// Unlocks a vault with the given password.
        /// </summary>
        /// <returns>True if the vault is unlocked succesfully, false otherwise.</returns>
        public Task<bool> UnlockVault(string vault, string password);
        public void LockVault();

        /// <summary>
        /// Gets the content of a vault. <br/>
        /// The vault that should be returned is managed by the class that implements this interface. (the currently unlocked vault)
        /// </summary>
        public Task<Vault> GetVault();

        /// <summary>
        /// Creates a new vault with the given vault name and password and stores the symmetric key
        /// </summary>
        /// <returns>True on success, false otherwise.</returns>
        public Task<bool> CreateNewVault(string vault, string password, string psk);

        /// <summary>
        /// Delets a vault. <br/>
        /// The vault that should be deleted is managed by the class that implements this interface. (the currently unlocked vault)
        /// </summary>
        /// <returns></returns>
        public Task<bool> DeleteVault();

        /// <summary>
        /// Adds a new item in the vault. If an item with the same name is already in the database it is overwritten.
        /// To edit an item use <see cref="EditItemInVault{T}(T, T)"/>. <br/>
        /// <typeparamref name="T"/> is <see cref="PasswordItem"/> => passwords field <br/>
        /// <typeparamref name="T"/> is <see cref="NoteItem"/> => notes field <br/>
        /// </summary>
        public Task<bool> AddItemInVault<T>(T item) where T: notnull;

        /// <summary>
        /// <typeparamref name="T"/> is <see cref="PasswordItem"/> => passwords field <br/>
        /// <typeparamref name="T"/> is <see cref="NoteItem"/> => notes field <br/>
        /// </summary>
        public Task<bool> EditItemInVault<T>(T oldItem, T newItem) where T : notnull;

        /// <summary>
        /// <typeparamref name="T"/> is <see cref="PasswordItem"/> => passwords field <br/>
        /// <typeparamref name="T"/> is <see cref="NoteItem"/> => notes field <br/>
        /// </summary>
        public Task<bool> DeleteItemFromVault<T>(T item) where T : notnull;
        
        public Task<bool> SetGeneratorHistory(List<string> history);
    }
}
