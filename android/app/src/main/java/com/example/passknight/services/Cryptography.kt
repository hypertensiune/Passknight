package com.example.passknight.services

import android.util.Base64
import java.security.SecureRandom

class Cryptography(email: String, password: String, protectedSymmetricKey: String) {

    private var symmetricKey: ByteArray

    class Utils {

        companion object {

            const val ITERATIONS = 600000

            /**
             * PBKDF2 with SHA_256
             * @param key The key to be derived
             * @param keyLength The length of the derived key in bytes
             */
            external fun pbkdf2(key: ByteArray, salt: ByteArray, iterations: Int, keyLength: Int): ByteArray

            /**
             * HKDF with SHA_512
             *
             * @param ikm The input key material on which HKDF is done
             * @param keyLength The length of the output key in bytes
             */
            external fun hkdf(ikm: ByteArray, salt: ByteArray, keyLength: Int): ByteArray

            /**
             * @param out Make sure its length is the length of input + 16.
             * @return The length of the out array on success, -1 otherwise
             */
            external fun aesencrypt(key: ByteArray, input: ByteArray, iv: ByteArray, out: ByteArray): Int

            /**
             * @param out Make sure its length is the length of input + 16
             * @return The length of the out array on success, -1 otherwise
             */
            external fun aesdecrypt(key: ByteArray, input: ByteArray, iv: ByteArray, out: ByteArray): Int

            /**
             * Generates a master key by derivating the password with PBKDF2 and the email as a salt.
             * The master password hash is obtained by derivating the master key again with PBKDF2 and
             * the original password a salt
             */
            @JvmStatic
            fun getMasterPasswordHash(email: String, password: String): String {
                val masterKey = pbkdf2(password.toByteArray(), email.toByteArray(), ITERATIONS, 32)
                val masterPasswordHash = pbkdf2(masterKey, password.toByteArray(), ITERATIONS, 32)

                return Base64.encodeToString(masterPasswordHash, Base64.NO_WRAP)
            }


            /**
             * Creates the symmetric key used for encryption and decryption and generates the master password hash used for firebase authentication.
             * (Used when a vault is created)
             * This process is influenced by Bitwarden's security paper.
             *
             * @return The master password hash that is used for authentication with firebase account and the
             * protected symmetric key that is used with encrypting and decrypting as base64 strings
             *
             * @see <a href="https://bitwarden.com/help/bitwarden-security-white-paper/">Bitwarden's security paper</a>.
             */
            @JvmStatic
            fun create(email: String, password: String): Pair<String, String> {

                val masterKey = pbkdf2(password.toByteArray(), email.toByteArray(), ITERATIONS, 32)
                val stretchedMasterKey = hkdf(masterKey, email.toByteArray(), 32)

                // Generate a 256 bit symmetric key and a 128 bit initialization vector
                // and encrypt the symmetric key using the stretched master key
                val secureRandom = SecureRandom.getInstanceStrong()

                val generatedSymmetricKey = ByteArray(32)
                secureRandom.nextBytes(generatedSymmetricKey)
                val iv = ByteArray(16)
                secureRandom.nextBytes(iv)

                val protectedSymmetricKey = ByteArray(32 + 16)
                val size = aesencrypt(stretchedMasterKey, generatedSymmetricKey, iv, protectedSymmetricKey)

                val masterPasswordHash = getMasterPasswordHash(email, password)

                // Concatenate the iv and the protected symmetric key before encoding to base64
                return Pair(masterPasswordHash, Base64.encodeToString(iv + protectedSymmetricKey.sliceArray(0..<size), Base64.NO_WRAP))
            }
        }
    }

    init {
        val masterKey = Utils.pbkdf2(password.toByteArray(), email.toByteArray(), Utils.ITERATIONS, 32)
        val stretchedMasterKey = Utils.hkdf(masterKey, email.toByteArray(), 32)

        val protectedSymmetricKeyBytes = Base64.decode(protectedSymmetricKey, Base64.NO_WRAP)

        val iv = protectedSymmetricKeyBytes.slice(0..15).toByteArray()
        val psk = protectedSymmetricKeyBytes.slice(16..< protectedSymmetricKeyBytes.size).toByteArray()

        val symmetricKey = ByteArray(psk.size + 16)
        val size = Utils.aesdecrypt(stretchedMasterKey, psk, iv, symmetricKey)

        this.symmetricKey = symmetricKey.sliceArray(0..<size)
    }

    /**
     * @param input UTF-8 encoded string
     * @return The encrypted string encoded as base64
     */
    fun encrypt(input: String): String {
        val inputBytes = input.toByteArray()

        val iv = ByteArray(16)
        SecureRandom.getInstanceStrong().nextBytes(iv)

        val encrypted = ByteArray(inputBytes.size + 16)
        val size = Utils.aesencrypt(symmetricKey, inputBytes, iv, encrypted)

        return Base64.encodeToString(iv + encrypted.sliceArray(0..<size), Base64.NO_WRAP)
    }

    /**
     * @param input base64 encoded string
     * @return UTF-8 decrypted string
     */
    fun decrypt(input: String): String {
        val decoded = Base64.decode(input, Base64.NO_WRAP)

        val iv = decoded.slice(0..15).toByteArray()
        val bytes = decoded.slice(16..<decoded.size).toByteArray()

        val decrypted = ByteArray(bytes.size + 16)
        val size = Utils.aesdecrypt(symmetricKey, bytes, iv, decrypted)

        return String(decrypted, 0, size, Charsets.UTF_8)
    }
}