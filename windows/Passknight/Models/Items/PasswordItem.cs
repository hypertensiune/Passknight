using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Models.Items
{
    internal class PasswordItem : ICryptable
    {
        public string Name { get; set; }
        public string Password { get; set; } = String.Empty;
        public string Username { get; set; }
        public string Website { get; set; }

        public void Decrypt(Func<string, string> decrypt)
        {
            Password = decrypt(Password);
        }

        public void Encrypt(Func<string, string> encrypt)
        {
            Password = encrypt(Password);
        }

        public PasswordItem Clone() => (PasswordItem)this.MemberwiseClone();
    }
}
