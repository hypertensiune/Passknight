/**
 * Provides various cryptography functions in native code using openssl for better performance
 * compared to Java and Kotlin's default implementations.
 */

#include <jni.h>
#include <string>

#include "openssl/digest.h"
#include "openssl/evp.h"
#include "openssl/hkdf.h"
#include "openssl/aes.h"

#define BLOCK_SIZE 16

extern "C" JNIEXPORT jbyteArray JNICALL Java_com_example_passknight_services_Cryptography_00024Utils_00024Companion_pbkdf2(JNIEnv *env, jobject object, jbyteArray key, jbyteArray salt, jint iterations, jint key_length)
{
    uint8_t* key_bytes = (uint8_t*)env->GetByteArrayElements(key, NULL);
    jsize key_size = env->GetArrayLength(key);

    uint8_t* salt_bytes = (uint8_t*)env->GetByteArrayElements(salt, NULL);
    jsize salt_size = env->GetArrayLength(salt);

    uint8_t derived_key[key_length];
    PKCS5_PBKDF2_HMAC((const char*)key_bytes, key_size, salt_bytes, salt_size, (int)iterations, EVP_sha256(), (int)key_length, derived_key);

    jbyteArray result = env->NewByteArray((int)key_length);
    env->SetByteArrayRegion(result, 0, 32, reinterpret_cast<const jbyte*>(derived_key));

    env->ReleaseByteArrayElements(key, (jbyte*)key_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(salt, (jbyte*)salt_bytes, JNI_ABORT);

    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL Java_com_example_passknight_services_Cryptography_00024Utils_00024Companion_hkdf(JNIEnv *env, jobject object, jbyteArray ikm, jbyteArray salt, jint key_length)
{
    uint8_t* ikm_bytes = (uint8_t*)env->GetByteArrayElements(ikm, NULL);
    jsize ikm_size = env->GetArrayLength(ikm);

    uint8_t* salt_bytes = (uint8_t*)env->GetByteArrayElements(salt, NULL);
    jsize salt_size = env->GetArrayLength(salt);

    uint8_t out_key[key_length];
    HKDF(out_key, key_length, EVP_sha512(), ikm_bytes, ikm_size, salt_bytes, salt_size, NULL, 0);

    jbyteArray result = env->NewByteArray((int)key_length);
    env->SetByteArrayRegion(result, 0, (int)key_length, reinterpret_cast<const jbyte*>(out_key));

    env->ReleaseByteArrayElements(ikm, (jbyte*)ikm_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(salt, (jbyte*)salt_bytes, JNI_ABORT);

    return result;
}

extern "C" JNIEXPORT jint JNICALL Java_com_example_passknight_services_Cryptography_00024Utils_00024Companion_aesencrypt(JNIEnv *env, jobject object, jbyteArray key, jbyteArray input, jbyteArray iv, jbyteArray out)
{
    // https://wiki.openssl.org/index.php/EVP_Symmetric_Encryption_and_Decryption

    uint8_t* key_bytes = (uint8_t*)env->GetByteArrayElements(key, NULL);
    uint8_t* iv_bytes = (uint8_t*)env->GetByteArrayElements(iv, NULL);
    uint8_t* input_bytes = (uint8_t*)env->GetByteArrayElements(input, NULL);
    jsize input_size = env->GetArrayLength(input);

    EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
    if(!ctx)
        return -1;

    if(EVP_EncryptInit_ex(ctx, EVP_aes_256_cbc(), NULL, key_bytes, iv_bytes) != 1)
        return -1;

    uint8_t* encrypted = new uint8_t[input_size + BLOCK_SIZE];
    int len, encrypted_len;

    if(EVP_EncryptUpdate(ctx, encrypted, &len, input_bytes, (int)input_size) != 1)
        return -1;
    encrypted_len = len;

    if(EVP_EncryptFinal_ex(ctx, encrypted + len, &len) != 1)
        return -1;
    encrypted_len += len;

    EVP_CIPHER_CTX_free(ctx);

    env->SetByteArrayRegion(out, 0, encrypted_len, reinterpret_cast<const jbyte*>(encrypted));

    env->ReleaseByteArrayElements(key, (jbyte*)key_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(input, (jbyte*)input_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(iv, (jbyte*)iv_bytes, JNI_ABORT);

    delete[] encrypted;

    return encrypted_len;
}

extern "C" JNIEXPORT jint JNICALL Java_com_example_passknight_services_Cryptography_00024Utils_00024Companion_aesdecrypt(JNIEnv *env, jobject object, jbyteArray key, jbyteArray input, jbyteArray iv, jbyteArray out)
{
    // https://wiki.openssl.org/index.php/EVP_Symmetric_Encryption_and_Decryption

    uint8_t* key_bytes = (uint8_t*)env->GetByteArrayElements(key, NULL);
    uint8_t* iv_bytes = (uint8_t*)env->GetByteArrayElements(iv, NULL);
    uint8_t* input_bytes = (uint8_t*)env->GetByteArrayElements(input, NULL);
    jsize input_size = env->GetArrayLength(input);

    EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
    if(!ctx)
        return -1;

    if(EVP_DecryptInit_ex(ctx, EVP_aes_256_cbc(), NULL, key_bytes, iv_bytes) != 1)
        return -2;

    uint8_t* decrypted = new uint8_t[input_size + BLOCK_SIZE];
    int len, decrypted_len;

    if(EVP_DecryptUpdate(ctx, decrypted, &len, input_bytes, (int)input_size) != 1)
        return -3;
    decrypted_len = len;

    if(EVP_DecryptFinal_ex(ctx, decrypted + len, &len) != 1)
        return -4;
    decrypted_len += len;

    EVP_CIPHER_CTX_free(ctx);

    env->SetByteArrayRegion(out, 0, decrypted_len, reinterpret_cast<const jbyte*>(decrypted));

    env->ReleaseByteArrayElements(key, (jbyte*)key_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(input, (jbyte*)input_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(iv, (jbyte*)iv_bytes, JNI_ABORT);

    delete[] decrypted;

    return decrypted_len;
}