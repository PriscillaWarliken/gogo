package com.translate.app.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.translate.app.App
import com.translate.app.ads.AdManager
import com.translate.app.ads.base.AdWrapper
import com.translate.app.repository.bean.ConfigBean
import com.translate.app.repository.bean.LanguageBeanItem
import com.translate.app.ui.ImagePickerActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Repository {

    lateinit var sharedPreferences:SharedPreferences
    var sourceLanguage by mutableStateOf<LanguageBeanItem?>(value = null)
    var targetLanguage by mutableStateOf<LanguageBeanItem?>(value = null)
    var recentLanguageList = mutableListOf<LanguageBeanItem>()
    var allLanguageList = mutableListOf<LanguageBeanItem>()

    fun init() {
        sharedPreferences = App.context.getSharedPreferences(App.context.packageName,Context.MODE_PRIVATE)
    }

    fun setRecentLanguage(languageBeanItem: LanguageBeanItem) {
        if (recentLanguageList.size >= 4 && recentLanguageList.contains(languageBeanItem)) {
            recentLanguageList.remove(languageBeanItem)
        }
        if (recentLanguageList.size >= 4 && recentLanguageList.contains(languageBeanItem).not()) {
            recentLanguageList.removeLast()
        }
        recentLanguageList.add(0,languageBeanItem)
    }

    suspend fun parseLanguageJson() = withContext(Dispatchers.IO){
        try {
            App.context.assets.open("language.json").use {
                val json = it.readBytes().decodeToString()
                val result = Gson().fromJson<List<LanguageBeanItem>>(json, object : TypeToken<List<LanguageBeanItem>>() {}.type)
                allLanguageList.addAll(result)
                val default = allLanguageList.filter { (it.languageEn.contains("English") || it.languageEn.contains("Hindi") || it.languageEn.contains("Portuguese") || it.languageEn.contains("Spanish")) }
                default.forEach {
                    setRecentLanguage(it)
                    if (it.languageEn.contains("English")){
                        sourceLanguage = it
                        ImagePickerActivity.setSourceLanguageMethod(it.languageEn)
                    }
                    if (it.languageEn.contains("Hindi")){
                        targetLanguage = it
                        ImagePickerActivity.setTargetLanguageMethod(it.languageEn)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getSearchResult(searchStr: String): List<LanguageBeanItem> {
        return allLanguageList.filter { it.languageEn.contains(searchStr, true) }
    }

    private lateinit var configBean:ConfigBean

    suspend fun useCacheConfig() = withContext(Dispatchers.IO){
        try {
            App.context.openFileInput("configTxt").use {
                configBean = Gson().fromJson(it.readBytes().decodeToString(), ConfigBean::class.java)
                dealConfig()
            }
            Log.d("ConfigLog","缓存配置成功")
        } catch (e: Exception) {
            Log.d("ConfigLog","缓存配置失败:${e.message}")
            useLocalConfig()
        }
        launch { getConfigApi() }
    }

    private suspend fun useLocalConfig() = withContext(Dispatchers.IO) {
        try {
            App.context.assets.open("config.json").use {
                val json = it.readBytes().decodeToString()
                configBean = Gson().fromJson(json, ConfigBean::class.java)
                dealConfig()
            }
            Log.d("ConfigLog", "使用本地配置成功:${configBean}")
        } catch (e: Exception) {
            Log.d("ConfigLog", "使用本地配置失败:${e.message}")
        }
    }

    private fun dealConfig() {
        configBean.resp.adArrays?.let {
            val map = mutableMapOf<String, AdWrapper>()
            it.forEach { out ->
                out.adSource[0].let {
                    map[out.advPlace] = AdWrapper(out.advPlace,out.adOpen,out.adSource.sortedByDescending { it.adv_scale })
                }
            }
            AdManager.initAdMapConfig(map, 3)
        }
    }

    suspend fun getConfigApi() = withContext(Dispatchers.IO) {
        try {
            configBean = ServiceCreator.api.getConfigOnline()
            saveConfigBean()
            dealConfig()
        } catch (e: Exception) {
            Log.d("TAG", "getConfigApi:${e.message}")
        }
    }
    private suspend fun saveConfigBean() = withContext(Dispatchers.IO){
        try {
            val jsonBytes = Gson().toJson(configBean).encodeToByteArray()
            App.context.openFileOutput("configTxt", Context.MODE_PRIVATE).use {
                it.flush()
                it.write(jsonBytes)
            }
        } catch (_: Exception) { }
    }

}