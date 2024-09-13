using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Models.Items
{
    internal class PasswordItem : Item, ICryptable
    {
        public override string Name { get; set; }
        public string Password { get; set; } = String.Empty;
        public string Username { get; set; } = String.Empty;
        public string Website { get; set; } = String.Empty;
        public override string Created { get; set; }
        public override string Updated { get; set; }

        public void Decrypt(Func<string, string> decrypt)
        {
            Website = decrypt(Website);
            Username = decrypt(Username);
            Password = decrypt(Password);
        }

        public void Encrypt(Func<string, string> encrypt)
        {
            Website = encrypt(Website);
            Username = encrypt(Username);
            Password = encrypt(Password);
        }

        public PasswordItem Clone() => (PasswordItem)this.MemberwiseClone();
    }
}
