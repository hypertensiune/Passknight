using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Cryptography;

using System.Text;
using System.Threading.Tasks;

namespace Passknight.Services
{
    /// <summary>
    /// Cryptography provider for encryption and decryption with AES-CBC.
    /// </summary>
    class Cryptography
    {
        private byte[] key;

        private static string ByteToBase64(byte[] data)
        {
            return System.Convert.ToBase64String(data);
        }

        private static byte[] Base64ToByte(string data)
        {
            return System.Convert.FromBase64String(data);
        }

        private static byte[] StringToByte(string data)
        {
            return System.Text.Encoding.UTF8.GetBytes(data);
        }

        private static string ByteToString(byte[] data)
        {
            return System.Text.Encoding.UTF8.GetString(data);
        }

        /// <summary>
        /// Generate a random 32 byte salt.
        /// </summary>
        /// <returns>The generated salt as a base64 string.</returns>
        public static string GenerateSalt()
        {
            var salt = RandomNumberGenerator.GetBytes(32);
            return ByteToBase64(salt);
        }

        /// <summary>
        /// Derive the key from the vault's master password and its salt.
        /// Uses <see cref="Rfc2898DeriveBytes.Pbkdf2(byte[], byte[], int, HashAlgorithmName, int)"/> with 600.000 iterations of SHA256.
        /// </summary>
        public void Initialize(string masterPassword, string salt)
        {
            key = Rfc2898DeriveBytes.Pbkdf2(StringToByte(masterPassword), StringToByte(salt), 600000, HashAlgorithmName.SHA256, 32);
        }

        /// <summary>
        /// Encrypt the input using AES-CBC.
        /// </summary>
        /// <returns>
        /// The encrypted string in base64.
        /// </returns>
        public string Encrypt(string input)
        {
            Aes aes = Aes.Create();

            aes.Mode = CipherMode.CBC;
            aes.Padding = PaddingMode.PKCS7;
            aes.KeySize = 256;
            aes.Key = key;

            byte[] inputBytes = StringToByte(input);

            byte[] encryptedBytes;
            using(ICryptoTransform encryptor =  aes.CreateEncryptor())
            {
                encryptedBytes = encryptor.TransformFinalBlock(inputBytes, 0, inputBytes.Length);
            }

            var IV = aes.IV;
            return ByteToBase64(IV.Concat(encryptedBytes).ToArray());
        }

        /// <summary>
        /// Decrypt the base64 string returned by <see cref="Encrypt(string)"/>. 
        /// </summary>
        /// <returns>
        /// The decrypted string in UTF-8 encoding.
        /// </returns>
        public string Decrypt(string input)
        {
            Aes aes = Aes.Create();

            aes.Mode = CipherMode.CBC;
            aes.KeySize = 256;
            aes.Key = key;
            aes.Padding = PaddingMode.PKCS7;

            byte[] inputBytes = Base64ToByte(input);

            byte[] IV = new byte[16];
            Array.Copy(inputBytes, 0, IV, 0, 16);

            aes.IV = IV;

            byte[] bytesToDecrypt = new byte[inputBytes.Length - 16];
            Array.Copy(inputBytes, 16, bytesToDecrypt, 0, inputBytes.Length - 16);

            byte[] decryptedBytes;
            using (ICryptoTransform decryptor = aes.CreateDecryptor())
            {
                decryptedBytes = decryptor.TransformFinalBlock(bytesToDecrypt, 0, bytesToDecrypt.Length);
            }

            return ByteToString(decryptedBytes);
        }
    }
}
