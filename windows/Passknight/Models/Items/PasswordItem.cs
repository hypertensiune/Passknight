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
        public string Created { get; set; } = String.Empty;
        public string Updated { get; set; } = String.Empty;

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
