//
// Created by feng on 10/01/2023.
//
#include <cstdlib>
#include <cstring>
#include "crypto.h"
#include "des.h"

unsigned char * printHex(unsigned char *data , int len){
    int i;
    int hex_len = len * 2;
    static const char *hex = "0123456789ABCDEF";
    unsigned char *out = (unsigned char *) malloc(hex_len + 1);
    memset(out, 0, hex_len);

    for (i = 0; i < len; i++)
    {
        char f = hex[(data[i] >> 4) & 0x0f];
        char s = hex[data[i] & 0x0f];
        out[i * 2] = f;
        out[i * 2 + 1] = s;
    }

    out[hex_len] = 0;
    return out;
}

int indexOf(char c){
    static const char *temp = "0123456789ABCDEF";
    int len = strlen(temp);
    for (int i = 0; i < len; i++) {
        if (temp[i] == c) {
            return i;
        }
    }
    return -1;
}

unsigned char * hexStringToBytes(unsigned char *data , int * hex_len){
    * hex_len = *hex_len / 2;
    auto *out = (unsigned char *) malloc(*hex_len);

    for (int i = 0; i < *hex_len; i++) {
        int pos = i * 2;
        int f = indexOf(data[pos]) << 4;
        int s = indexOf(data[pos + 1]);
        if (f == -1 || s == -1)
            return nullptr;
        out[i] = f | s;
    }
    return out;
}

unsigned char* unpack_padding_pkcs5(const unsigned char in[], int *len){
    unsigned char paddNum = in[*len - 1];
    if(paddNum > 8){
        *len = 0;
        return NULL;
    }
    *len = *len - paddNum;

    unsigned char *data = (unsigned char *)malloc(*len);
    memset(data, 0, *len);
    memcpy(data, in, *len );
    return data;
}

unsigned char* pack_padding_pkcs5(const unsigned char in[], int *len){
    unsigned char paddNum = 8 - *len % 8;
    unsigned char *data = (unsigned char *)malloc(*len + paddNum);
    int i = 0;
    memset(data, 0, *len + paddNum);
    memcpy(data, in, *len);
    for (i = 0; i < paddNum; i++) {
        data[*len + i] = paddNum;
    }
    *len = *len + paddNum;
    return data;
}

//des加密
void des_encode(CRYPTO_TYPE type, const unsigned char key[], const unsigned char vi[8], const unsigned char in[],unsigned char **out,int *len){
    unsigned char *data = pack_padding_pkcs5(in,len);
    *out = (unsigned char *)malloc(*len);
    mbedtls_des_context context;
    mbedtls_des_init(&context);
    mbedtls_des_setkey_enc(&context, key);

    unsigned char v[8] = {0};
    memcpy(v,vi,8);
    if(type == DES_ECB){
        my_des_cry_ebc(&context,MBEDTLS_DES_ENCRYPT,*len, data, *out);
    } else if (type == DES_CBC) {
        mbedtls_des_crypt_cbc(&context,MBEDTLS_DES_ENCRYPT,*len, v, data, *out);
    }
    free(data);
}

//des解密
void des_decode(CRYPTO_TYPE type, const unsigned char key[], const unsigned char vi[8], const unsigned char in[],unsigned char **out,int *len){

    if(*len % 8){
        return;
    }
    unsigned char *data = (unsigned char *)malloc(*len);

    mbedtls_des_context context;
    mbedtls_des_init(&context);
    mbedtls_des_setkey_dec(&context, key);
    unsigned char v[8] = {0};
    memcpy(v,vi,8);

    if(type == DES_ECB){
        my_des_cry_ebc(&context,MBEDTLS_DES_DECRYPT,*len, in, data);
    } else if (type == DES_CBC) {
        mbedtls_des_crypt_cbc(&context,MBEDTLS_DES_DECRYPT,*len, v, in, data);
    }
    *out = unpack_padding_pkcs5(data,len);
    free(data);
    if(*len == 0){
        return;
    }
    return;
}

static const unsigned char to_b64[] =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

static const unsigned char un_b64[] = {
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 62,  255, 255, 255, 63,
        52,  53,  54,  55,  56,  57,  58,  59,  60,  61,  255, 255, 255, 255, 255, 255,
        255, 0,   1,   2,   3,   4,   5,   6,   7,   8,   9,   10,  11,  12,  13,  14,
        15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25,  255, 255, 255, 255, 255,
        255, 26,  27,  28,  29,  30,  31,  32,  33,  34,  35,  36,  37,  38,  39,  40,
        41,  42,  43,  44,  45,  46,  47,  48,  49,  50,  51,  255, 255, 255, 255, 255,
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255
};

#define UNSIG_CHAR_PTR(x) ((const unsigned char *)(x))
unsigned char *acl_base64_encode( const char *in, size_t len){
    const unsigned char *cp;
    int     count, size = len * 4 /3;

    unsigned char * out = (unsigned char *)malloc(size + 1);
    int out_index = 0;
    for (cp = UNSIG_CHAR_PTR(in), count = len; count > 0; count -= 3, cp += 3) {
        out[out_index++] = to_b64[cp[0] >> 2];
        if (count > 1) {
            out[out_index++] = to_b64[(cp[0] & 0x3) << 4 | cp[1] >> 4];
            if (count > 2) {
                out[out_index++] = to_b64[(cp[1] & 0xf) << 2 | cp[2] >> 6];
                out[out_index++] = to_b64[cp[2] & 0x3f];
            }else{
                out[out_index++] = to_b64[(cp[1] & 0xf) << 2];
                out[out_index++] = '=';
                break;
            }
        } else {
            out[out_index++] = to_b64[(cp[0] & 0x3) << 4];
            out[out_index++] = '=';
            out[out_index++] = '=';
            break;
        }
    }
    out[out_index] = 0;
    return out;
}

unsigned char *acl_base64_decode(const char *in, int* len){
    const unsigned char *cp;
    int     count;
    int     ch0;
    int     ch1;
    int     ch2;
    int     ch3;

    /*
 * Sanity check.
 */
    if (*len % 4)
        return (NULL);

#define INVALID		0xff

    unsigned char * out = (unsigned char *)malloc(*len);
    int out_index = 0;
    for (cp = UNSIG_CHAR_PTR(in), count = 0; count < *len; count += 4) {
        if ((ch0 = un_b64[*cp++]) == INVALID|| (ch1 = un_b64[*cp++]) == INVALID)
            return (0);
        out[out_index++] = ch0 << 2 | ch1 >> 4;
        if ((ch2 = *cp++) == '=')
            break;
        if ((ch2 = un_b64[ch2]) == INVALID)
            return (0);
        out[out_index++] = ch1 << 4 | ch2 >> 2;
        if ((ch3 = *cp++) == '=')
            break;
        if ((ch3 = un_b64[ch3]) == INVALID)
            return (0);
        out[out_index++] = ch2 << 6 | ch3;
    }

    out[out_index] = 0;
    *len = out_index;
    return out;
}
