package com.translate.app.repository

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.translate.app.BuildConfig
import com.translate.app.Const
import com.translate.app.repository.bean.ConfigBean
import com.translate.app.repository.bean.ResultBean
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface Apis {
    companion object {
        const val config_url = BuildConfig.config_url
        const val translate_url = BuildConfig.translate_url
    }

    @POST(config_url)
    suspend fun getConfigOnline(@Body jsonObject: JsonObject = Const.baseParam): ConfigBean

    @POST(translate_url)
    suspend fun getTranslateResult(@Body jsonObject: JsonObject): ResultBean


}

object ServiceCreator {

    private const val baseUrl = BuildConfig.base_url

    private val httpClient by lazy {
        OkHttpClient.Builder().apply {
            readTimeout(60, TimeUnit.SECONDS)
            writeTimeout(60, TimeUnit.SECONDS)
            connectTimeout(60, TimeUnit.SECONDS)
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            }
            addInterceptor(TInterceptor())
        }.build()
    }

    val api: Apis by lazy {
        Retrofit.Builder().apply {
            client(httpClient)
            baseUrl(baseUrl)
            addConverterFactory(GsonConverterFactory.create())
        }.build().create(Apis::class.java)
    }
}