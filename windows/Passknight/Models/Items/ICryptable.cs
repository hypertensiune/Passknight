using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Models.Items
{
    interface ICryptable
    {
        public void Encrypt(Func<string, string> encrypt);
        public void Decrypt(Func<string, string> decrypt);
    }
}
