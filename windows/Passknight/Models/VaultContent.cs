using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Models
{
    class VaultContent
    {
        public List<PasswordItem> PasswordItems { get; }
        public List<NoteItem> NoteItems { get; }
        public List<string> GeneratorHistory { get; }
        public string Salt { get; }

        public VaultContent(List<PasswordItem> passwordItems, List<NoteItem> noteItems, List<string> generatorHistory, string salt)
        {
            PasswordItems = passwordItems;
            NoteItems = noteItems;
            GeneratorHistory = generatorHistory;
            Salt = salt;
        }
    }
}
