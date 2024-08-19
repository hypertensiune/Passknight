using System.Security.Cryptography;

namespace Passknight.Services.Cryptography
{
    /// <summary>
    /// Cryptography provider for encryption and decryption with AES-CBC.
    /// </summary>
    class Cryptography
    {
        private byte[] symmetricKey;

        /// <summary>
        /// Initializes the cryptography service with the generated symmetric key.
        /// </summary>
        public Cryptography(byte[] symmetricKey)
        {
            this.symmetricKey = symmetricKey;
        }


        /// <summary>
        /// Decrypts the protected symmetric key to obtain the symmetric key
        /// required for encryption and decryption.
        /// </summary>
        /// <param name="email"></param>
        /// <param name="password"></param>
        /// <param name="protectedSymmetricKey"></param>
        public Cryptography(string email, string password, string protectedSymmetricKey)
        {
            byte[] masterKey = Rfc2898DeriveBytes.Pbkdf2(Cryptoutils.StringToByte(password), Cryptoutils.StringToByte(email), 600000, HashAlgorithmName.SHA256, 32);
            byte[] stretchedMasterKey = HKDF.DeriveKey(HashAlgorithmName.SHA512, masterKey, 32, Cryptoutils.StringToByte(email));

            byte[] protectedSymmetricKeyBytes = Cryptoutils.Base64ToByte(protectedSymmetricKey);

            byte[] iv = new byte[16];
            Array.Copy(protectedSymmetricKeyBytes, 0, iv, 0, 16);

            byte[] psk = new byte[protectedSymmetricKeyBytes.Length - 16];
            Array.Copy(protectedSymmetricKeyBytes, 16, psk, 0, protectedSymmetricKeyBytes.Length - 16);

            Aes aes = Aes.Create();

            aes.Mode = CipherMode.CBC;
            aes.Padding = PaddingMode.PKCS7;
            aes.Key = stretchedMasterKey;
            aes.IV = iv;

            byte[] symmetricKey;
            using(ICryptoTransform decryptor = aes.CreateDecryptor())
            {
                symmetricKey = decryptor.TransformFinalBlock(psk, 0, psk.Length);
            }

            this.symmetricKey = symmetricKey;
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
            aes.Key = symmetricKey;

            byte[] inputBytes = Cryptoutils.StringToByte(input);

            byte[] encryptedBytes;
            using (ICryptoTransform encryptor = aes.CreateEncryptor())
            {
                encryptedBytes = encryptor.TransformFinalBlock(inputBytes, 0, inputBytes.Length);
            }

            var IV = aes.IV;
            return Cryptoutils.ByteToBase64(IV.Concat(encryptedBytes).ToArray());
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
            aes.Key = symmetricKey;
            aes.Padding = PaddingMode.PKCS7;

            byte[] inputBytes = Cryptoutils.Base64ToByte(input);

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

            return Cryptoutils.ByteToString(decryptedBytes);
        }
    }
}
