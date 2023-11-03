package com.translate.app.ads

import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustEvent
import com.translate.app.App
import com.translate.app.BuildConfig
import com.translate.app.Const
import com.translate.app.ads.base.AdWrapper
import com.translate.app.ads.base.IntAd
import com.translate.app.ads.base.NavAd
import com.translate.app.ads.base.OpenAd
import com.translate.app.ads.callback.AdCallBack
import com.translate.app.ads.callback.FullAdCallback
import com.translate.app.ads.callback.SmallAdCallback
import com.translate.app.repository.Repository
import java.util.TimeZone
import java.util.concurrent.ConcurrentLinkedQueue

object AdManager {

    private const val TAG = "AdLog"
    lateinit var mAdConfigMap: MutableMap<String, AdWrapper>

    val smallPoolPool = ConcurrentLinkedQueue<AdWrapper>()
    val fullPoolPool = ConcurrentLinkedQueue<AdWrapper>()

    private var smallCallMap = mutableMapOf<String, SmallAdCallback>()

    private lateinit var mFullCallBack: FullAdCallback

    var skipTag = mutableListOf<String>()
    var skipLiveData = MutableLiveData<MutableList<String>>()

    var needShowNav:Boolean = false
    private var permissionNav: Boolean = true
    private var navClickCount:Int = 3
    private var nowClickCount:Int = Repository.sharedPreferences.getInt(Const.AdConst.CLICK_COUNT,0)

    private var adRequestCode = BuildConfig.adRequestCode
    private var adTapCode = BuildConfig.adTapCode
    private var adFillCode = BuildConfig.adFillCode

    private var adUnit = BuildConfig.adUnit
    private var adRevenue = BuildConfig.adRevenue
    private var adSite = BuildConfig.adSite


    fun setFullCallBack(l: FullAdCallback) {
        this.mFullCallBack = l
    }

    fun setSmallCallBack(l: SmallAdCallback,place: String) {
        smallCallMap.clear()
        smallCallMap[place] = l
    }

    fun clearSmallCallBack() {
        needShowNav = false
        smallCallMap.clear()
    }

    private fun requestEvent(adWrapper: AdWrapper, place: String) {
        val event = AdjustEvent(adRequestCode)
        event.addCallbackParameter(adUnit, adWrapper.id)
        event.addCallbackParameter(adRevenue,"admob")
        event.addCallbackParameter(adSite,place)
        Adjust.trackEvent(event)
    }

    private fun clickEvent(adWrapper: AdWrapper, place: String) {
        val event = AdjustEvent(adTapCode)
        event.addCallbackParameter(adUnit, adWrapper.id)
        event.addCallbackParameter(adRevenue,"admob")
        event.addCallbackParameter(adSite,place)
        Adjust.trackEvent(event)
    }

    private fun fillEvent(adWrapper: AdWrapper, place: String) {
        val event = AdjustEvent(adFillCode)
        event.addCallbackParameter(adUnit, adWrapper.id)
        event.addCallbackParameter(adRevenue,"admob")
        event.addCallbackParameter(adSite,place)
        Adjust.trackEvent(event)
    }

    fun initAdMapConfig(admap:MutableMap<String, AdWrapper>, clickLimit:Int = 3) {
        try {
            if (this::mAdConfigMap.isInitialized.not()) {
                mAdConfigMap = admap
                return
            }
            mAdConfigMap.map {
                it.value.place = admap[it.key]!!.place
                it.value.openBtn = admap[it.key]!!.openBtn
                it.value.innerAdList = admap[it.key]!!.innerAdList
            }
            resetUpNavLimit(clickLimit)
        }catch (_:Exception){}
    }

    private fun resetUpNavLimit(navClickCount: Int) {
        this.navClickCount = navClickCount
        this.nowClickCount = Repository.sharedPreferences.getInt(Const.AdConst.CLICK_COUNT,0)
        val clickTime = Repository.sharedPreferences.getLong(Const.AdConst.CLICK_TIME, 0L)
        permissionNav = Repository.sharedPreferences.getBoolean(Const.AdConst.canLoadNav,true)

        if (!permissionNav && getTodayTime() - clickTime >= 1) {
            this.nowClickCount = 0
            permissionNav = true
            Repository.sharedPreferences.edit {
                putBoolean(Const.AdConst.canLoadNav, true)
                putLong(Const.AdConst.CLICK_TIME, 0L)
                putInt(Const.AdConst.CLICK_COUNT, 0)
            }
        }
    }

    fun loadAdInstance(vararg adPlace:String) {
        if (this::mAdConfigMap.isInitialized.not()) {
            return
        }

        adPlace.forEach out@{place->
            when (place) {
                in listOf(Const.AdConst.AD_START, Const.AdConst.AD_INSERT) -> {
                    if (fullPoolPool.any { it.place == place }){
                        return@out
                    }
                }
                in listOf(Const.AdConst.AD_OTHER,Const.AdConst.AD_TEXT,Const.AdConst.AD_INITIAL) -> {
                    if (smallPoolPool.any { it.place == place }){
                        return@out
                    }
                }
            }


            mAdConfigMap.let {
                if (it[place]?.openBtn == true){
                    loadAdInstancePlace(it[place]!!,place)
                }else{
                    Log.d(TAG, "$place 广告位关闭 不请求")
                    if (skipTag.contains(Const.AdConst.AD_TEXT).not() && place == Const.AdConst.AD_TEXT) {
                        skipTag.add(Const.AdConst.AD_TEXT)
                        skipLiveData.postValue(skipTag)
                    }
                    if (skipTag.contains(Const.AdConst.AD_START).not() && place == Const.AdConst.AD_START) {
                        skipTag.add(Const.AdConst.AD_START)
                        skipLiveData.postValue(skipTag)
                    }
                }
            }
        }
    }

    private fun loadAdInstancePlace(adWrapper: AdWrapper, place: String, index:Int = 0) {

        if (adWrapper.adLoading) {
            Log.d(TAG, "$place 正在请求中---> 等待回调结果")
            return
        }

        adWrapper.id = adWrapper.innerAdList[index].ad_code
        adWrapper.type = adWrapper.innerAdList[index].advFormat
        adWrapper.weight = adWrapper.innerAdList[index].adv_scale

        if (adWrapper.type == Const.AdConst.TYPE_NAV && permissionNav.not()) {
            Log.d(TAG, "原生限制---->不请求 ")
            if (skipTag.contains(Const.AdConst.AD_TEXT).not() && place == Const.AdConst.AD_TEXT) {
                skipTag.add(Const.AdConst.AD_TEXT)
                skipLiveData.postValue(skipTag)
            }
            return
        }

        adWrapper.adLoading = true
        Log.d(TAG, "-------------------- 请求 $place 广告 id:${adWrapper.id} 权重:${adWrapper.weight} 类型:${adWrapper.type} index:${index}--------------------")

        requestEvent(adWrapper,place)

        when (adWrapper.type) {
            Const.AdConst.TYPE_INT -> {
                loadIntAdInstance(index,place,adWrapper)
            }
            Const.AdConst.TYPE_OPEN -> {
                loadOpenAdInstance(index,place,adWrapper)
            }
            Const.AdConst.TYPE_NAV -> {
                loadNavAdInstance(index,place,adWrapper)
            }
            else->{
                Log.d(TAG,"$place 类型:${adWrapper.type} 错误")
                adWrapper.adLoading = false
            }
        }
    }


    private fun loadOpenAdInstance(index: Int, place: String ,adWrapper: AdWrapper) {
        OpenAd().apply {
            setMyAdCallBack(object :AdCallBack{
                override fun onLoadSuccess(adInstance: Any) {
                    fillEvent(adWrapper, place)

                    adWrapper.setAdInstance(adInstance)
                    fullPoolPool.add(adWrapper)
                    Log.d(TAG,"$place 请求成功 全屏广告缓存池里数量:${fullPoolPool.size}, 权重:${adWrapper.weight}, index:${index},hashCode:" + adInstance.hashCode())

                    if (place == Const.AdConst.AD_START && skipTag.contains(Const.AdConst.AD_START).not()) {
                        skipTag.add(Const.AdConst.AD_START)
                        skipLiveData.postValue(skipTag)
                    }
                }

                override fun onLoadFail(code: String, msg: String) {
                    Log.d(TAG,"$place 请求失败 msg:$msg id:${adWrapper.id} 权重:${adWrapper.weight} ,index:${index} ")
                    adWrapper.adLoading = false
                    if (index > adWrapper.innerAdList.size - 2) {
                        if (place == Const.AdConst.AD_START && skipTag.contains(Const.AdConst.AD_START).not()) {
                            skipTag.add(Const.AdConst.AD_START)
                            skipLiveData.postValue(skipTag)
                        }
                        return
                    }
                    loadAdInstancePlace(adWrapper, place, index + 1)
                }

                override fun onShow() {
                    Log.d(TAG,"$place 展示成功 id:${adWrapper.id}, 权重:${adWrapper.weight} hashCode:${adWrapper.getAdInstance().hashCode()}")
                    loadAdInstance(place)

                }

                override fun onClose() {
                    mFullCallBack.onCloseFull()
                }

                override fun onClick() {
                    clickEvent(adWrapper,place)
                }
            })
        }.loadAd(adWrapper.id)
    }

    private fun loadNavAdInstance(
        index: Int,
        place: String,
        adWrapper: AdWrapper
    ) {
        NavAd().apply {
            setMyAdCallBack(object :AdCallBack{
                override fun onLoadSuccess(adInstance: Any) {
                    fillEvent(adWrapper,place)

                    adWrapper.setAdInstance(adInstance)
                    smallPoolPool.add(adWrapper)
                    Log.d(TAG,"$place 请求成功 小屏广告缓存池里数量:${smallPoolPool.size}, 权重:${adWrapper.weight}, index:${index},hashCode:" + adInstance.hashCode())

                    if (needShowNav && App.isBackground.not()) {
                        smallCallMap[place]?.let {
                            it.getSmallFromPool(adWrapper)
                            smallPoolPool.remove(adWrapper)
                            clearSmallCallBack()
                        }
                        return
                    }

                    if (place == Const.AdConst.AD_TEXT && skipTag.contains(Const.AdConst.AD_TEXT).not()) {
                        skipTag.add(Const.AdConst.AD_TEXT)
                        skipLiveData.postValue(skipTag)
                    }
                }

                override fun onLoadFail(code: String, msg: String) {
                    Log.d(TAG,"$place 请求失败 msg:$msg id:${adWrapper.id} 权重:${adWrapper.weight} ,index:${index} ")
                    adWrapper.adLoading = false
                    if (index > adWrapper.innerAdList.size - 2) {
                        if (place == Const.AdConst.AD_TEXT && skipTag.contains(Const.AdConst.AD_TEXT).not()) {
                            skipTag.add(Const.AdConst.AD_TEXT)
                            skipLiveData.postValue(skipTag)
                        }
                        return
                    }
                    loadAdInstancePlace(adWrapper, place, index + 1)
                }

                override fun onShow() {
                    Log.d(TAG,"$place 展示成功 id:${adWrapper.id}, 权重:${adWrapper.weight} hashCode:${adWrapper.getAdInstance().hashCode()}")
                    clearSmallCallBack()
                    loadAdInstance(place)
                }

                override fun onClose() {}

                override fun onClick() {
                    clickEvent(adWrapper,place)

                    nowClickCount++
                    Repository.sharedPreferences.edit {
                        putInt(Const.AdConst.CLICK_COUNT, nowClickCount)
                        putLong(Const.AdConst.CLICK_TIME, System.currentTimeMillis())
                    }
                    if (nowClickCount >= navClickCount) {
                        permissionNav = false
                        Repository.sharedPreferences.edit { putBoolean(Const.AdConst.canLoadNav, permissionNav) }
                    }

                }
            })
        }.loadAd(adWrapper.id)

    }

    private fun loadIntAdInstance(index: Int,place: String, adWrapper: AdWrapper) {
        IntAd().apply {
            setMyAdCallBack(object : AdCallBack {
                override fun onLoadSuccess(adInstance: Any) {
                    fillEvent(adWrapper,place)

                    adWrapper.setAdInstance(adInstance)
                    fullPoolPool.add(adWrapper)
                    Log.d(TAG,"$place 请求成功 全屏广告缓存池里数量:${fullPoolPool.size}, 权重:${adWrapper.weight}, index:${index},hashCode:" + adInstance.hashCode())
                    if (place == Const.AdConst.AD_START && skipTag.contains(Const.AdConst.AD_START).not()) {
                        skipTag.add(Const.AdConst.AD_START)
                        skipLiveData.postValue(skipTag)
                    }
                }

                override fun onLoadFail(code: String, msg: String) {
                    Log.d(TAG,"$place 请求失败 msg:$msg id:${adWrapper.id} 权重:${adWrapper.weight} ,index:${index} ")
                    adWrapper.adLoading = false
                    if (index > adWrapper.innerAdList.size - 2) {
                        if (place == Const.AdConst.AD_START && skipTag.contains(Const.AdConst.AD_START)
                                .not()
                        ) {
                            skipTag.add(Const.AdConst.AD_START)
                            skipLiveData.postValue(skipTag)
                        }
                        return
                    }
                    loadAdInstancePlace(adWrapper, place, index + 1)
                }

                override fun onShow() {
                    Log.d(TAG,"$place 展示成功 id:${adWrapper.id}, 权重:${adWrapper.weight} hashCode:${adWrapper.getAdInstance().hashCode()}")
                    loadAdInstance(place)
                }

                override fun onClose() {
                    mFullCallBack.onCloseFull()
                }

                override fun onClick() {
                    clickEvent(adWrapper,place)
                }
            })
        }.loadAd(adWrapper.id)
    }

    fun getAdInstanceFromPool(adLocation: String) {
        try {
            if ((!::mAdConfigMap.isInitialized)) {
                if (adLocation == Const.AdConst.AD_START || adLocation == Const.AdConst.AD_INSERT) {
                    mFullCallBack.getFullFromPool(null)
                }
                return
            }

            if (mAdConfigMap[adLocation]?.openBtn == false) {
                if (adLocation == Const.AdConst.AD_START || adLocation == Const.AdConst.AD_INSERT) {
                    mFullCallBack.getFullFromPool(null)
                }
                return
            }

            if (App.isBackground) {
                Log.d(TAG, "禁止在后台获取广告")
                if (adLocation == Const.AdConst.AD_START || adLocation == Const.AdConst.AD_INSERT) {
                    mFullCallBack.getFullFromPool(null)
                }
                return
            }

            val isSmallAdConst = adLocation in listOf(Const.AdConst.AD_TEXT, Const.AdConst.AD_OTHER,Const.AdConst.AD_INITIAL)
            val pool = if (!isSmallAdConst) fullPoolPool else smallPoolPool
            needShowNav = false

            if (pool.none { it.place == adLocation }) {
                mAdConfigMap[adLocation]?.let {
                    if (it.openBtn) {
                        if (adLocation == Const.AdConst.AD_TEXT || adLocation == Const.AdConst.AD_OTHER || adLocation == Const.AdConst.AD_INITIAL) {
                            needShowNav = true
                        }
                        loadAdInstancePlace(it, adLocation)
                    } else {
                        Log.d(TAG, adLocation + "广告位关闭，不请求")
                    }
                }
            }

            if (!isSmallAdConst) {
                val sortList = fullPoolPool.sortedByDescending { it.weight }
                if (sortList.isEmpty()) {
                    mFullCallBack.getFullFromPool(null)
                    return
                }
                val lowAdWeight = sortList.last().weight
                sortList.forEach {
                    if (adLocation != Const.AdConst.AD_START && it.type == Const.AdConst.TYPE_OPEN) {
                        return@forEach
                    }

                    if (sortList.size == 1) {
                        mFullCallBack.getFullFromPool(it)
                        fullPoolPool.remove(it)
                        return
                    }

                    if (it.weight > lowAdWeight) {
                        mFullCallBack.getFullFromPool(it)
                        fullPoolPool.remove(it)
                        return
                    }
                }

                val adWrapper: AdWrapper? = fullPoolPool.find { it.place == adLocation }
                adWrapper?.let {
                    mFullCallBack.getFullFromPool(it)
                    fullPoolPool.remove(it)
                    return
                }
                mFullCallBack.getFullFromPool(null)
            } else {
                val sortList = smallPoolPool.sortedByDescending { it.weight }
                val lowAdWeight = sortList.last().weight
                sortList.forEach {
                    if (it.weight > lowAdWeight) {
                        smallPoolPool.remove(it)
                        smallCallMap[adLocation]?.getSmallFromPool(it)
                        return
                    }
                }

                val adWrapper: AdWrapper? = smallPoolPool.find { it.place == adLocation }
                adWrapper?.let {
                    smallPoolPool.remove(it)
                    smallCallMap[adLocation]?.getSmallFromPool(it)
                    return
                }

            }
        } catch (e: Exception) {
            Log.d(TAG, "获取广告:" + e.message + " ,广告位:" + adLocation)
        }
    }

    fun loadingAdPool() {
        Log.d(TAG, "-----------------> 检查广告池 <-----------------")
        clearSkipTag()
        loadAdInstance(Const.AdConst.AD_START, Const.AdConst.AD_INSERT, Const.AdConst.AD_INITIAL,Const.AdConst.AD_OTHER, Const.AdConst.AD_TEXT)
    }

    private fun clearSkipTag() {
        skipTag.clear()
        if (fullPoolPool.any { it.place == Const.AdConst.AD_START}){
            skipTag.add(Const.AdConst.AD_START)
        }

        if (smallPoolPool.any { it.place == Const.AdConst.AD_TEXT }){
            skipTag.add(Const.AdConst.AD_TEXT)
        }
    }

}

fun getTodayTime(): Long {
    val times = System.currentTimeMillis()
    return times - (times + TimeZone.getDefault().rawOffset) % (24 * 60 * 60 * 1000L)
}