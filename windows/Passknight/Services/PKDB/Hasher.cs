using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Services.PKDB
{
    internal class Hasher
    {
        /// <summary>
        /// Hashes a string and returns the hash as a base64 string.
        /// </summary>
        /// <returns></returns>
        public static string Hash(string input)
        {
            byte[] bytes = System.Text.Encoding.UTF8.GetBytes(input);
            byte[] hash = SHA256.HashData(bytes);
            
            return Convert.ToBase64String(hash);
        }

        /// <param name="input">base64 input string.</param>
        /// <param name="hash">The hash to verify against.</param>
        public static bool Verify(string input, string hash)
        {
            var hashed = Hash(input);

            return String.Equals(hashed, hash);
        }
    }
}
