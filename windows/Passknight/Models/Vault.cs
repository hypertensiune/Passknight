using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Models
{
    class Vault
    {
        public string Name { get; }
        public List<PasswordItem> PasswordItems { get; }
        public List<NoteItem> NoteItems {get; }
        public List<string> GeneratorHistory {get; }
        public string Salt {get; }

        public Vault(string name, VaultContent content)
        {
            Name = name;
            PasswordItems = content.PasswordItems;
            NoteItems = content.NoteItems;
            GeneratorHistory = content.GeneratorHistory;
            Salt = content.Salt;
        }
    }
}
