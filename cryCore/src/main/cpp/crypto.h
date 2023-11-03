#ifndef __CRYPTO_H
#define __CRYPTO_H


typedef enum{
	DES_ECB =0,
	DES_CBC,
	DES3_ECB,
	DES3_CBC,
}CRYPTO_TYPE;


typedef enum{
	RESULT_TYPE_BIN =0,//bin类型
	RESULT_TYPE_STR_UPPER,//HEX大写字符串
	RESULT_TYPE_STR_LOWER,//HEX小写字符串
}MD5_RESULT_TYPE;


typedef enum{
	RESULT_OK = 0,
	RESULT_ERROR,
}CRYPTO_RESULT;
	

#ifdef __cplusplus
extern "C" {
#endif

/*
	DES 加密
	type[in]:加密方式
	key[in] 秘钥 des 固定8位 3des可为8位 16位 24位
	vi[in] 偏移   固定8位， CBC方式可用,ECB 为null
	in[in] 待加密数据
	out[out] 加密后数据(需要free)
	len[in/out] 传入待加密数据长度，传出加密后数据长度
	return 是否成功
*/
void des_encode(CRYPTO_TYPE type, const unsigned char key[], const unsigned char vi[8], const unsigned char in[],unsigned char **out,int *len);
	
	
/*
	DES 解密
	type[in]:解密方式
	key[in] 秘钥 des 固定8位 3des可为8位 16位 24位
	vi[in] 偏移	固定8位， CBC方式可用,ECB 为null
	in[in] 待解密数据
	out[out] 解密后数据(需要free)
	len[in/out] 传入待解密数据长度，传出解密后数据长度
	return 是否成功
*/
void des_decode(CRYPTO_TYPE type, const unsigned char key[], const unsigned char vi[8], const unsigned char in[],unsigned char **out,int *len);


//pkcs5填充
unsigned char *unpack_padding_pkcs5(const unsigned char in[], int *len);
unsigned char *pack_padding_pkcs5(const unsigned char in[], int *len);

//转16进制
unsigned char * printHex(unsigned char *data , int len);
unsigned char * hexStringToBytes(unsigned char *data , int * hex_len);

/**
     * BASE64 编码函数
     * @param in {const char*} 源数据
     * @param len {int} in 源数据的长度
     * @return unsigned char * 编码结果，需要free释放
     */
unsigned char *acl_base64_encode( const char *in, size_t len);


/**
 * BASE64 解码函数
 * @param in {const char*} 编码后的数据
 * @param len {int} in 数据长度
 * @return unsigned char * NULL: 解码失败; !=NULL: 解码成功需要free释放
 */
unsigned char *acl_base64_decode(const char *in, int* len);


#ifdef __cplusplus
}
#endif
#endif
