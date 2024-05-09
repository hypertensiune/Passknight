using Passknight.Models;
using Passknight.Models.Items;
using System.IO;
using System.Runtime.Intrinsics.Arm;
using System.Windows;
using System.Xml.Linq;

namespace Passknight.Services.PKDB
{
    /// <summary>
    /// Implements the <see cref="IDatabase"/> interface.
    /// </summary>
    internal class Database : IDatabase
    {
        private readonly Dictionary<string, string> _vaultPasswordHashes;

        private string _unlockedVault;

        struct OffsetData()
        {
            public long passwordItems = 0;
            public long noteItems = 0;
            public long history = 0;
        };

        private OffsetData offsetData;

        public Database()
        {
            _vaultPasswordHashes = new Dictionary<string, string>();

            // Initialize the file structure
            if(!Directory.Exists("pkdb"))
            {
                Directory.CreateDirectory("pkdb");
            }

            if(!File.Exists("pkdb/vaults"))
            {
                File.Create("pkdb/vaults");
            }
        }

        public Task<List<string>> GetVaultNames()
        {
            try
            {
                var stream = new FileStream("pkdb/vaults", FileMode.Open, FileAccess.Read);
                var reader = new BinaryReader(stream);
                
                var list = new List<string>();
                
                while(stream.Position != stream.Length)
                {
                    try
                    {
                        string vault = reader.ReadString();
                        string passwordHash = reader.ReadString();

                        _vaultPasswordHashes.TryAdd(vault, passwordHash);
                        list.Add(vault);

                    } catch(EndOfStreamException)
                    {
                        break;
                    }
                }

                return Task.FromResult(list);

            } catch(FileNotFoundException)
            {
                Msgbox.Show("Error", "pkdb/vaults not found!");
                return Task.FromResult(new List<string>());
            }
        }

        public Task<bool> UnlockVault(string vault, string password)
        {
            string hash = _vaultPasswordHashes[vault];
            if (Hasher.Verify(password, hash))
            {
                _unlockedVault = vault;
                return Task.FromResult(true);
            }

            return Task.FromResult(false);
        }

        public Task<bool> CreateNewVault(string name, string password)
        {
            if(_vaultPasswordHashes.ContainsKey(name))
            {
                Msgbox.Show("Error", "Vault already exists!");
                return Task.FromResult(false);
            }

            try
            {
                var stream = new FileStream("pkdb/vaults", FileMode.Open, FileAccess.ReadWrite);

                // Add the new vault to the pkdb/vaults file
                _vaultPasswordHashes.Add(name, Hasher.Hash(password));
                using (var writer = new BinaryWriter(stream))
                {
                    foreach (var pair in _vaultPasswordHashes)
                    {
                        writer.Write(pair.Key);
                        writer.Write(pair.Value);
                    }
                    writer.Flush();
                    writer.Close();
                    stream.Dispose();
                }

                // Create the actual vault file and initialize it with a salt
                // and 3 zeros that represent the length of the password, note and history items
                var file = File.Create($"pkdb/{name}.pkvault");
                using (var writer = new BinaryWriter(file))
                {
                    writer.Write(Cryptography.GenerateSalt());
                    writer.Write(0);
                    writer.Write(0);
                    writer.Write(0);
                    writer.Flush();
                    writer.Close();
                    file.Dispose();
                }

                // Set the new vault as the unlocked one
                _unlockedVault = name;
                
                return Task.FromResult(true);

            } catch (FileNotFoundException)
            {
                Msgbox.Show("Error", "There was a problem creating a new vault!");
                return Task.FromResult(false);
            }
        }

        public void LockVault()
        {
            _unlockedVault = string.Empty;
        }

        public Task<Vault> GetVault()
        {
            List<PasswordItem> passwordItems = new List<PasswordItem>();
            List<NoteItem> noteItems = new List<NoteItem>();
            List<string> history = new List<string>();

            string salt = "";

            try
            {
                var stream = new FileStream($"pkdb/{_unlockedVault}.pkvault", FileMode.Open, FileAccess.Read);
                var reader = new BinaryReader(stream);

                try
                {
                    salt = reader.ReadString();

                    // Save the current position as the starting point for the password items
                    offsetData.passwordItems = stream.Position;

                    int numberOfPasswordItems = reader.ReadInt32();
                    for (int i = 0; i < numberOfPasswordItems; i++)
                    {
                        var name = reader.ReadString();
                        var website = reader.ReadString();
                        var username = reader.ReadString();
                        var password = reader.ReadString();

                        passwordItems.Add(new PasswordItem()
                        {
                            Name = name,
                            Website = website,
                            Username = username,
                            Password = password
                        });
                    }

                    // Save the current position as the starting point for the note items
                    offsetData.noteItems = stream.Position;

                    int numberOfNoteItems = reader.ReadInt32();
                    for (int i = 0; i < numberOfNoteItems; i++)
                    {
                        var name = reader.ReadString();
                        var note = reader.ReadString();

                        noteItems.Add(new NoteItem()
                        {
                            Name = name,
                            Content = note
                        });
                    }

                    // Save the current position as the starting point for the history items
                    offsetData.history = stream.Position;

                    int historyLength = reader.ReadInt32();
                    for (int i = 0; i < historyLength; i++)
                    {
                        var item = reader.ReadString();
                        history.Add(item);
                    }
                }
                catch
                {
                    // If there is any problem reading the vault it means it's corrupted.
                    Msgbox.Show("Error!", "Corrupted vault!");
                }
            } catch
            {
                // There was a problem opening the vault file. Probably it's missing.
                Msgbox.Show("Error!", "Missing vault!");
            }

            VaultContent content = new VaultContent(passwordItems, noteItems, history, salt);

            return Task.FromResult(new Vault(_unlockedVault, content));
        }

        public Task<bool> DeleteVault()
        {
            try
            {
                File.Delete($"pkdb/{_unlockedVault}.pkvault");

                _vaultPasswordHashes.Remove(_unlockedVault);
                var stream = new FileStream("pkdb/vaults", FileMode.Create, FileAccess.Write);

                using (var writer = new BinaryWriter(stream))
                {
                    foreach (var pair in _vaultPasswordHashes)
                    {
                        writer.Write(pair.Key);
                        writer.Write(pair.Value);
                    }
                    writer.Flush();
                }

                return Task.FromResult(true);
            } catch
            {
                Msgbox.Show("Error", "There was a problem deleting this vault");
                return Task.FromResult(false);
            }
        }

        public Task<bool> UpdateFieldInVault<T>(List<T> items)
        {
            try
            {
                var stream = new FileStream($"pkdb/{_unlockedVault}.pkvault", FileMode.Open, FileAccess.ReadWrite);
                var writer = new BinaryWriter(stream);
                var reader = new BinaryReader(stream);

                if (typeof(T) == typeof(PasswordItem))
                {
                    // Read the bytes that follow after the password items.
                    stream.Seek(offsetData.noteItems, SeekOrigin.Begin);
                    var bytes = reader.ReadBytes((int)(stream.Length - stream.Position));

                    // Start writing the new password items at their offset
                    stream.Seek(offsetData.passwordItems, SeekOrigin.Begin);
                    writer.Write(items.Count);
                    foreach (var item in items)
                    {
                        writer.Write((item as PasswordItem).Name);
                        writer.Write((item as PasswordItem).Website);
                        writer.Write((item as PasswordItem).Username);
                        writer.Write((item as PasswordItem).Password);
                    }

                    // The other offsets need to be updated to account for the change
                    long offset = stream.Position - offsetData.noteItems;
                    offsetData.noteItems += offset;
                    offsetData.history += offset;

                    writer.Write(bytes);
                    writer.Flush();
                }
                if (typeof(T) == typeof(NoteItem))
                {
                    // Read the bytes that follow after the note items.
                    stream.Seek(offsetData.history, SeekOrigin.Begin);
                    var bytes = reader.ReadBytes((int)(stream.Length - stream.Position));

                    // Start writing the new note items at their offset
                    stream.Seek(offsetData.noteItems, SeekOrigin.Begin);
                    writer.Write(items.Count);
                    foreach (var item in items)
                    {
                        writer.Write((item as NoteItem).Name);
                        writer.Write((item as NoteItem).Content);
                    }

                    // The other offset needs to be updated to account for the change
                    long offset = stream.Position - offsetData.noteItems;
                    offsetData.history += offset;

                    writer.Write(bytes);
                    writer.Flush();
                }
                if (typeof(T) == typeof(string))
                {
                    // Start writing the new history items at their offset
                    stream.Seek(offsetData.history, SeekOrigin.Begin);
                    writer.Write(items.Count);
                    foreach (var item in items)
                    {
                        writer.Write(item as string);
                    }
                }

                writer.Flush();
                return Task.FromResult(true);
            } catch
            { 
                return Task.FromResult(false);
            }
        }
    }
}
