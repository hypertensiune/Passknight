using System.Security.Cryptography;
using System.Text;

namespace Passknight.Services.Cryptography
{
    static class Cryptoutils
    {
        public static string ByteToBase64(byte[] data)
        {
            return Convert.ToBase64String(data);
        }

        public static byte[] Base64ToByte(string data)
        {
            return Convert.FromBase64String(data);
        }

        public static byte[] StringToByte(string data)
        {
            return Encoding.UTF8.GetBytes(data);
        }

        public static string ByteToString(byte[] data)
        {
            return Encoding.UTF8.GetString(data);
        }


        /// <summary>
        /// Generates a master key by derivating the password with PBKDF2 and the email as a salt.
        /// The master password hash is obtained by derivating the master key again with PBKDF2 and
        /// the original password a salt
        /// </summary>
        public static string GetMasterPasswordHash(string email, string password)
        {
            byte[] masterKey = Rfc2898DeriveBytes.Pbkdf2(StringToByte(password), StringToByte(email), 600000, HashAlgorithmName.SHA256, 32);
            byte[] masterPasswordHash = Rfc2898DeriveBytes.Pbkdf2(masterKey, StringToByte(password), 600000, HashAlgorithmName.SHA256, 32);

            return ByteToBase64(masterPasswordHash);
        }

        /// <summary>
        /// Creates the symmetric key used for encryption and decryption and generates the master password hash used for firebase authentication.
        /// (Used when a vault is created)
        /// This process is influenced by Bitwarden's security paper. For more details see <a href="https://bitwarden.com/help/bitwarden-security-white-paper/"/>.
        /// </summary>
        /// <returns>
        /// The master password hash that is used for authentication with firebase account and the 
        /// protected symmetric key that is used with encrypting and decrypting as base64 strings
        /// </returns>
        public static (string, string) Create(string email, string password)
        {
            byte[] masterKey = Rfc2898DeriveBytes.Pbkdf2(StringToByte(password), StringToByte(email), 600000, HashAlgorithmName.SHA256, 32);
            byte[] stretchedMasterKey = HKDF.DeriveKey(HashAlgorithmName.SHA512, masterKey, 32, StringToByte(email));

            // Generate a 256 bit symmetric key and a 128 bit initialization vector
            // and encrypt the symmetric key using the stretched master key
            byte[] generatedSymmetricKey = RandomNumberGenerator.GetBytes(32);
            byte[] iv = RandomNumberGenerator.GetBytes(16);

            Aes aes = Aes.Create();

            aes.Mode = CipherMode.CBC;
            aes.Padding = PaddingMode.PKCS7;
            aes.Key = stretchedMasterKey;
            aes.IV = iv;

            byte[] protectedSymmetricKey;
            using(ICryptoTransform encryptor =  aes.CreateEncryptor())
            {
                protectedSymmetricKey = encryptor.TransformFinalBlock(generatedSymmetricKey, 0, generatedSymmetricKey.Length);
            }

            string masterPasswordHash = GetMasterPasswordHash(email, password);

            return (masterPasswordHash, ByteToBase64(iv.Concat(protectedSymmetricKey).ToArray()));
        }
    }
}
