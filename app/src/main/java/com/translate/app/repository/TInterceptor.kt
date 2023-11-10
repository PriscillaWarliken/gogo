package com.translate.app.repository

import android.util.Base64
import com.translate.app.App
import com.translate.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.security.MessageDigest

class TInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val mRequest = chain.request()
            if (mRequest.method.equals("get", true)) {
                return chain.proceed(mRequest.newBuilder().get().build())
            }

            val buffer = okio.Buffer()
            mRequest.body?.writeTo(buffer)
            var data = buffer.readByteArray()
            if (data.isEmpty()) {
                return chain.proceed(chain.request().newBuilder().url("http://localhost").get().build())
            }

            val sign = (if (BuildConfig.DEBUG) "9eniVOQC/0ryrsNj0UeoyviTj0M=" else getSign()).encodeToByteArray()
            val result = ByteArray(sign.size + data.size + 1)
            result[0] = sign.size.toByte()
            System.arraycopy(sign, 0, result, 1, sign.size)
            System.arraycopy(data, 0, result, sign.size + 1, data.size)

            val mResponse = chain.proceed(mRequest.newBuilder().apply {
                addHeader("Content-Type", "application/octet-stream")
                post(data.toRequestBody(mRequest.body?.contentType(), 0, data.size))
            }.build())

            if (mResponse.isSuccessful.not()) {
                return mResponse
            }

            var decryResponse: Response
            mResponse.body?.let {responseBody ->
                data = responseBody.bytes()
                if (data.isNotEmpty()) {
                    decryResponse = mResponse.newBuilder().body(data.toResponseBody(responseBody.contentType())).build()
                    return decryResponse
                }
            }

            return mResponse
        } catch (e: IOException) {
            e.printStackTrace()
            return chain.proceed(chain.request().newBuilder().url("http://localhost").get().build())
        }
    }

    fun getSign(): String {
        return try {
            val data = App.context.packageManager.getPackageInfo(App.context.packageName, 64).signatures[0].toByteArray()
            val m = MessageDigest.getInstance("SHA")
            m.update(data)
            val digest = m.digest()
            Base64.encodeToString(digest, 2) ?: ""
        } catch (_: Exception) {
            ""
        }
    }
}