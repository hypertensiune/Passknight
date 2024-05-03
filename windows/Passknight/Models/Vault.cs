using Passknight.Models.Items;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Models
{
    class Vault
    {
        private readonly VaultContent _content;

        public string Name { get; }
        public List<PasswordItem> PasswordItems => _content.PasswordItems;
        public List<NoteItem> NoteItems => _content.NoteItems;
        public List<string> GeneratorHistory => _content.GeneratorHistory;
        public string Salt => _content.Salt;

        public Vault(string name, VaultContent content)
        {
            Name = name;
            _content = content;
        }
    }
}
