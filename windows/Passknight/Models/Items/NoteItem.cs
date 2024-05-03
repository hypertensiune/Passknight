using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Models.Items
{
    internal class NoteItem : ICryptable
    {
        public string Name { get; set; }
        public string Content { get; set; }

        public void Decrypt(Func<string, string> decrypt)
        {
            Content = decrypt(Content);
        }

        public void Encrypt(Func<string, string> encrypt)
        {
            Content = encrypt(Content);
        }

        public NoteItem Clone() => (NoteItem)this.MemberwiseClone();
    }
}
