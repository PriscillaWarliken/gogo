package com.translate.app.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.translate.app.App
import com.translate.app.Const
import com.translate.app.ads.AdManager
import com.translate.app.ads.base.AdWrapper
import com.translate.app.repository.bean.ConfigBean
import com.translate.app.repository.bean.LanguageBeanItem
import com.translate.app.ui.ImagePickerActivity
import com.translate.app.ui.weight.getSpByTag
import com.translate.app.ui.weight.saveSP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Repository {

    lateinit var sharedPreferences:SharedPreferences
    var sourceLanguage by mutableStateOf<LanguageBeanItem?>(value = null)
    var targetLanguage by mutableStateOf<LanguageBeanItem?>(value = null)
    var recentLanguageList = mutableListOf<LanguageBeanItem>()
    var allLanguageList = mutableListOf<LanguageBeanItem>()
    var extraAd_button = false
    var firstAdOpen_button = false

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


        val json = Gson().toJson(recentLanguageList)
        sharedPreferences.edit {
            putString(Const.RECENT_LANGUAGE,json)
        }
    }

    suspend fun parseLanguageJson() = withContext(Dispatchers.IO){
        try {
            App.context.assets.open("language.json").use {
                val json = it.readBytes().decodeToString()
                val result = Gson().fromJson<List<LanguageBeanItem>>(json, object : TypeToken<List<LanguageBeanItem>>() {}.type)
                allLanguageList.addAll(result)

                val recentJson = "" //sharedPreferences.getString(Const.RECENT_LANGUAGE,"") ?: ""

                if (recentJson.isEmpty()) {
                    val default = allLanguageList.filter { (it.languageEn.contains("English") || it.languageEn.contains("Hindi") || it.languageEn.contains("Portuguese") || it.languageEn.contains("Spanish")) }
                    default.forEach {
                        setRecentLanguage(it)
                        if (it.languageEn.contains("English")){
                            it.saveSP(Const.SOURCE_LANGUAGE)
                            sourceLanguage = it
                            ImagePickerActivity.setSourceLanguageMethod(it.languageEn)
                        }
                        if (it.languageEn.contains("Hindi")){
                            it.saveSP(Const.TARGET_LANGUAGE)
                            targetLanguage = it
                            ImagePickerActivity.setTargetLanguageMethod(it.languageEn)
                        }
                    }
                }else{
                    val default = Gson().fromJson<List<LanguageBeanItem>>(recentJson, object : TypeToken<List<LanguageBeanItem>>() {}.type)
                    default.forEach{
                        setRecentLanguage(it)
                    }
                    Const.SOURCE_LANGUAGE.getSpByTag<LanguageBeanItem>()?.let {
                        sourceLanguage = it
                        ImagePickerActivity.setSourceLanguageMethod(it.languageEn)
                    }
                    Const.TARGET_LANGUAGE.getSpByTag<LanguageBeanItem>()?.let {
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
        if (searchStr.isEmpty()) {
            return emptyList()
        }
        return allLanguageList.filter { it.languageEn.contains(searchStr, true) }
    }

    private lateinit var configBean:ConfigBean
    private var refreshTime = 10 * 60 * 1000L

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
        launch { refreshConfig() }
    }

    private suspend fun refreshConfig() = withContext(Dispatchers.IO) {
        launch {
            while (isActive) {
                delay(refreshTime)
                getConfigApi()
            }
        }
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
        try {
            sharedPreferences.edit {
                putInt(Const.START_TIME,if (configBean.resp.bigBig == 0) 10 else configBean.resp.bigBig)
            }
            refreshTime = (if (configBean.resp.pullMin == 0) 10 else  configBean.resp.pullMin) * 60 * 1000L
            configBean.resp.adArrays?.let {
                val map = mutableMapOf<String, AdWrapper>()
                it.forEach { out ->
                    out.adSource[0].let {
                        map[out.advPlace] = AdWrapper(out.advPlace,out.adOpen,out.adSource.sortedByDescending { it.adv_scale },adType = it.advFormat)
                    }
                }
                AdManager.initAdMapConfig(map, configBean.resp.noNO)
            }

            extraAd_button = configBean.resp.extraAd_button
            firstAdOpen_button = configBean.resp.firstAdOpen_button
        }catch (e:Exception){
            e.printStackTrace()
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