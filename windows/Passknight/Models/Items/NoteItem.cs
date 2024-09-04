using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Models.Items
{
    internal class NoteItem : Item, ICryptable
    {
        public override string Name { get; set; }
        public string Content { get; set; }
        public override string Created { get; set; } = DateTimeOffset.Now.ToUnixTimeMilliseconds().ToString();
        public override string Updated { get; set; } = DateTimeOffset.Now.ToUnixTimeMilliseconds().ToString();

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
