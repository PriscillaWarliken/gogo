#include <jni.h>
#include <string>
#include <cstdio>
#include <cstdlib>
#include "aes.h"
#include "aes.c"
#include "des.h"
#include "crypto.h"

#define MODE_AES_ECB  0
#define MODE_DES_ECB  1
#define MODE_DES_CBC  2
#define MODE_BASE64 0
#define MODE_HEX 1
#define AES_KEY_SIZE 128

static const char *CRY_KEY = "YME0j1BfXF50UVWl";
static const char *CRY_IV = "";

static const char *DECRYPT_KEY = "K5iYOHYOaMtSuC5v";
static const char *DECRYPT_IV = "";

static jobject mContext;

const char* getSignature(JNIEnv *env, jobject context) {
    jclass native_class = env->GetObjectClass(context);
    jmethodID pm_id = env->GetMethodID(native_class, "getPackageManager",
                                       "()Landroid/content/pm/PackageManager;");
    jobject pm_obj = env->CallObjectMethod(context, pm_id);
    jclass pm_clazz = env->GetObjectClass(pm_obj);
    // 得到 getPackageInfo 方法的 ID
    jmethodID package_info_id = env->GetMethodID(pm_clazz, "getPackageInfo",
                                                 "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jclass native_classs = env->GetObjectClass(context);
    jmethodID mId = env->GetMethodID(native_classs, "getPackageName", "()Ljava/lang/String;");
    jstring pkg_str = static_cast<jstring>(env->CallObjectMethod(context, mId));
    // 获得应用包的信息
    jobject pi_obj = env->CallObjectMethod(pm_obj, package_info_id, pkg_str, 64);
    // 获得 PackageInfo 类
    jclass pi_clazz = env->GetObjectClass(pi_obj);
    // 获得签名数组属性的 ID
    jfieldID signatures_fieldId = env->GetFieldID(pi_clazz, "signatures",
                                                  "[Landroid/content/pm/Signature;");
    jobject signatures_obj = env->GetObjectField(pi_obj, signatures_fieldId);
    jobjectArray signaturesArray = (jobjectArray) signatures_obj;
    jobject signature_obj = env->GetObjectArrayElement(signaturesArray, 0);
    jclass signature_clazz = env->GetObjectClass(signature_obj);
    jmethodID toByteArray = env->GetMethodID(signature_clazz, "toByteArray", "()[B");
    jbyteArray data = static_cast<jbyteArray>(env->CallObjectMethod(signature_obj, toByteArray));

    // java/security/MessageDigest
    jclass messageDigestCls = env->FindClass("java/security/MessageDigest");
    jmethodID getInstance = env->GetStaticMethodID(messageDigestCls, "getInstance", "(Ljava/lang/String;)Ljava/security/MessageDigest;");
    jstring sh = env->NewStringUTF("SHA");
    jobject digestObj = env->CallStaticObjectMethod(messageDigestCls, getInstance, sh);
    jmethodID update = env->GetMethodID(messageDigestCls, "update", "([B)V");
    env->CallVoidMethod(digestObj, update, data);
    jmethodID digest = env->GetMethodID(messageDigestCls, "digest", "()[B");
    data = static_cast<jbyteArray>(env->CallObjectMethod(digestObj, digest));
    //android.util.Base64
    jclass base64 = env->FindClass("android/util/Base64");
    jmethodID encodeToString = env->GetStaticMethodID(base64, "encodeToString", "([BI)Ljava/lang/String;");
    jstring str = static_cast<jstring>(env->CallStaticObjectMethod(base64, encodeToString, data,2));
    const char *c_msg = env->GetStringUTFChars(str, 0);
    return c_msg;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_translate_crycore_CryUtil_init(JNIEnv *env, jobject thiz, jobject context) {
    if (mContext){
        env->DeleteGlobalRef(mContext);
        return nullptr;
    }
    mContext = env->NewGlobalRef(context);

    const char *sign = getSignature(env,mContext);
    jstring sign_str = env->NewStringUTF(sign);
    return sign_str;
}


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_translate_crycore_CryUtil_cry(JNIEnv *env, jobject thiz, jbyteArray byte_array,
                                  jint cry_mode, jint code_mode) {

    jsize byte_lenght = env->GetArrayLength(byte_array);
    auto *crypt = new uint8_t[byte_lenght];
    env->GetByteArrayRegion(byte_array, 0, byte_lenght,(jbyte *)(crypt));

    unsigned char *res = nullptr;

    switch (cry_mode) {
        case MODE_AES_ECB:
        {
            unsigned int key_schedule[AES_BLOCK_SIZE * 4] = {0};
            aes_key_setup((BYTE*)CRY_KEY, key_schedule, AES_KEY_SIZE);
            unsigned char *data = pack_padding_pkcs5(crypt,&byte_lenght);

            res = (unsigned char *)malloc(byte_lenght);
            aes_encrypt_ebc(data,byte_lenght,res,key_schedule,AES_KEY_SIZE);
        }break;

        case MODE_DES_ECB:
        {
            des_encode(DES_ECB, reinterpret_cast<const unsigned char *>(CRY_KEY),
                       reinterpret_cast<const unsigned char *>(CRY_IV),
                       crypt, &res, reinterpret_cast<int *>(&byte_lenght));
        }break;
        case MODE_DES_CBC:{
            des_encode(DES_CBC, reinterpret_cast<const unsigned char *>(CRY_KEY),
                       reinterpret_cast<const unsigned char *>(CRY_IV),
                       crypt, &res, reinterpret_cast<int *>(&byte_lenght));
        }break;
        default:break;
    }

    unsigned char * enc = nullptr;
    switch(code_mode){
        case MODE_BASE64:
            enc = acl_base64_encode(reinterpret_cast<const char *>(res), byte_lenght);
            break;
        case MODE_HEX:
            enc = printHex(res,byte_lenght);
            break;
        default:break;
    }

    jbyteArray result = env->NewByteArray(strlen(reinterpret_cast<const char *const>(enc)));
    env->SetByteArrayRegion(result, 0, strlen(reinterpret_cast<const char *const>(enc)), (jbyte*)enc);

    free(enc);
    return result;
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_translate_crycore_CryUtil_decry(JNIEnv *env, jobject thiz, jbyteArray byte_array,
                                    jint cry_mode, jint code_mode) {
    jsize byte_lenght = env->GetArrayLength(byte_array);
    auto *decrypt = new uint8_t[byte_lenght];
    env->GetByteArrayRegion(byte_array, 0, byte_lenght,(jbyte *)(decrypt));

    unsigned char * debase64 = nullptr;
    switch (code_mode) {
        case MODE_BASE64:
            debase64 = acl_base64_decode(reinterpret_cast<const char *>(decrypt),&byte_lenght);
            break;
        case MODE_HEX:
            debase64 = hexStringToBytes(decrypt, &byte_lenght);
            break;
        default:break;
    }

    unsigned char* res;
    switch (cry_mode) {
        case MODE_AES_ECB:
        {
            unsigned int key_schedule[AES_BLOCK_SIZE * 4] = {0};
            aes_key_setup((BYTE*)DECRYPT_KEY, key_schedule, AES_KEY_SIZE);

            res = (unsigned char *)malloc(byte_lenght);
            aes_decrypt_ebc(reinterpret_cast<const BYTE *>(debase64), byte_lenght, res, key_schedule, AES_KEY_SIZE);
            res = unpack_padding_pkcs5(res,&byte_lenght);
        }break;

        case MODE_DES_ECB:
        {
            des_decode(DES_ECB, reinterpret_cast<const unsigned char *>(DECRYPT_KEY),
                       reinterpret_cast<const unsigned char *>(DECRYPT_IV),
                       debase64, &res,
                       reinterpret_cast<int *>(&byte_lenght));
        }break;
        case MODE_DES_CBC:{
            des_decode(DES_CBC, reinterpret_cast<const unsigned char *>(DECRYPT_KEY),
                       reinterpret_cast<const unsigned char *>(DECRYPT_IV),
                       debase64, &res,
                       reinterpret_cast<int *>(&byte_lenght));
        }break;
        default:break;
    }

    jbyteArray result = env->NewByteArray(byte_lenght);
    env->SetByteArrayRegion(result, 0, byte_lenght, (jbyte*)res);

    free(debase64);
    free(res);
    return result;
}

