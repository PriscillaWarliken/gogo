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
import com.translate.app.ads.callback.IntAdCallback
import com.translate.app.ads.callback.NavAdCallback
import com.translate.app.repository.Repository
import java.util.TimeZone
import java.util.concurrent.ConcurrentLinkedQueue

object AdManager {

    private var adUnit = BuildConfig.adUnit
    private var adRevenue = BuildConfig.adRevenue
    private var adSite = BuildConfig.adSite
    private var adType = BuildConfig.adType
    private var adRequestEvent = BuildConfig.adRequestCode
    private var adTapEvent = BuildConfig.adTapCode
    private var adFillEvent = BuildConfig.adFillCode

    private const val TAG = "AdLog"
    private lateinit var mAdConfigMap: MutableMap<String, AdWrapper>

    private var nativeAdCallMap = mutableMapOf<String, NavAdCallback>()
    private var intAdCallBack: IntAdCallback ?= null

    val smallAdPool = ConcurrentLinkedQueue<AdWrapper>()
    val fullAdPool = ConcurrentLinkedQueue<AdWrapper>()

    var jumpTag = mutableListOf<String>()
    var jumpLiveData = MutableLiveData<MutableList<String>>()

    var adMapLiveData = MutableLiveData(false)
    var needShowNav:Boolean = false
    private var navPermission: Boolean = true
    private var navClickCount:Int = 3
    private var nowClickCount:Int = Repository.sharedPreferences.getInt(Const.AdConst.CLICK_COUNT,0)

    fun setNativeCallBack(l: NavAdCallback, place: String) {
        nativeAdCallMap.clear()
        nativeAdCallMap[place] = l
    }

    fun clearNativeCallBack() {
        needShowNav = false
        nativeAdCallMap.clear()
    }

    fun setIntAdCallBack(l: IntAdCallback) {
        this.intAdCallBack = l
    }

    fun clearIntAdCallBack() {
        intAdCallBack = null
    }

    private fun requestEvent(adWrapper: AdWrapper, place: String) {
        val event = AdjustEvent(adRequestEvent)
        event.addCallbackParameter(adUnit, adWrapper.id)
        event.addCallbackParameter(adRevenue,"admob")
        event.addCallbackParameter(adSite,place)
        event.addCallbackParameter(adType,adWrapper.type)
        Adjust.trackEvent(event)
    }

    private fun clickEvent(adWrapper: AdWrapper, place: String) {
        val event = AdjustEvent(adTapEvent)
        event.addCallbackParameter(adUnit, adWrapper.id)
        event.addCallbackParameter(adRevenue,"admob")
        event.addCallbackParameter(adSite,place)
        event.addCallbackParameter(adType,adWrapper.type)
        Adjust.trackEvent(event)
    }

    private fun fillEvent(adWrapper: AdWrapper, place: String) {
        val event = AdjustEvent(adFillEvent)
        event.addCallbackParameter(adUnit, adWrapper.id)
        event.addCallbackParameter(adRevenue,"admob")
        event.addCallbackParameter(adSite,place)
        event.addCallbackParameter(adType,adWrapper.type)
        Adjust.trackEvent(event)
    }

    fun initAdMapConfig(adMap:MutableMap<String, AdWrapper>, clickLimit:Int = 3) {
        try {
            if (this::mAdConfigMap.isInitialized.not()) {
                mAdConfigMap = adMap
                return
            }
            mAdConfigMap.map {
                it.value.place = adMap[it.key]!!.place
                it.value.openBtn = adMap[it.key]!!.openBtn
                it.value.innerAdList = adMap[it.key]!!.innerAdList
            }
            reloadNavClick(clickLimit)
            adMapLiveData.postValue(true)
        }catch (_:Exception){}
    }

    private fun reloadNavClick(navClickCount: Int) {
        this.navClickCount = navClickCount
        this.nowClickCount = Repository.sharedPreferences.getInt(Const.AdConst.CLICK_COUNT,0)
        val clickTime = Repository.sharedPreferences.getLong(Const.AdConst.CLICK_TIME, 0L)
        navPermission = Repository.sharedPreferences.getBoolean(Const.AdConst.permissionNav,true)

        if (!navPermission && getMoringTime() - clickTime >= 1) {
            this.nowClickCount = 0
            navPermission = true
            Repository.sharedPreferences.edit {
                putBoolean(Const.AdConst.permissionNav, true)
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
                    if (fullAdPool.any { it.place == place }){
                        return@out
                    }
                }
                in listOf(Const.AdConst.AD_OTHER,Const.AdConst.AD_TEXT,Const.AdConst.AD_INITIAL) -> {
                    if (smallAdPool.any { it.place == place }){
                        return@out
                    }
                }
            }
            mAdConfigMap.let {
                if (it[place]?.openBtn == true){
                    loadAdInstancePlace(it[place]!!,place)
                }else{
                    Log.d(TAG, "$place 广告位关闭 不请求")
                    if (jumpTag.contains(Const.AdConst.AD_TEXT).not() && place == Const.AdConst.AD_TEXT) {
                        jumpTag.add(Const.AdConst.AD_TEXT)
                        jumpLiveData.postValue(jumpTag)
                    }
                    if (jumpTag.contains(Const.AdConst.AD_START).not() && place == Const.AdConst.AD_START) {
                        jumpTag.add(Const.AdConst.AD_START)
                        jumpLiveData.postValue(jumpTag)
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
        adWrapper.type = adWrapper.innerAdList[index].advFormat
        if (adWrapper.type == Const.AdConst.TYPE_NAV && navPermission.not()) {
            Log.d(TAG, "原生限制---->不请求 ")
            if (jumpTag.contains(Const.AdConst.AD_TEXT).not() && place == Const.AdConst.AD_TEXT) {
                jumpTag.add(Const.AdConst.AD_TEXT)
                jumpLiveData.postValue(jumpTag)
            }
            return
        }

        adWrapper.adLoading = true
        Log.d(TAG, "-------------------- 请求 $place 广告 id:${adWrapper.id} 权重:${adWrapper.weight} 类型:${adWrapper.type} index:${index}--------------------")

        requestEvent(adWrapper,place)

        when (adWrapper.type) {
            Const.AdConst.TYPE_NAV -> {
                loadNavAdInstance(index,place,adWrapper)
            }
            Const.AdConst.TYPE_INT -> {
                loadIntAdInstance(index,place,adWrapper)
            }
            Const.AdConst.TYPE_OPEN -> {
                loadOpenAdInstance(index,place,adWrapper)
            }
            else->{
                Log.d(TAG,"$place 类型:${adWrapper.type} 错误")
                adWrapper.adLoading = false
            }
        }
    }

    private fun loadNavAdInstance(
        index: Int,
        place: String,
        adWrapper: AdWrapper
    ) {
        NavAd().apply {
            setAdCallBack(object :AdCallBack{
                override fun onLoadSuccess(adInstance: Any) {
                    fillEvent(adWrapper,place)

                    adWrapper.setAdInstance(adInstance)
                    smallAdPool.add(adWrapper)
                    Log.d(TAG,"$place 请求成功 小屏广告缓存池里数量:${smallAdPool.size}, 权重:${adWrapper.weight}, index:${index},hashCode:" + adInstance.hashCode())

                    if (needShowNav && App.isBackground.not()) {
                        nativeAdCallMap[place]?.let {
                            it.getNavAdFromPool(adWrapper)
                            smallAdPool.remove(adWrapper)
                            clearNativeCallBack()
                        }
                        return
                    }

                    if (place == Const.AdConst.AD_TEXT && jumpTag.contains(Const.AdConst.AD_TEXT).not()) {
                        jumpTag.add(Const.AdConst.AD_TEXT)
                        jumpLiveData.postValue(jumpTag)
                    }
                }

                override fun onLoadFail(code: String, msg: String) {
                    Log.d(TAG,"$place 请求失败 msg:$msg id:${adWrapper.id} 权重:${adWrapper.weight} ,index:${index} ")
                    adWrapper.adLoading = false
                    if (index > adWrapper.innerAdList.size - 2) {
                        if (place == Const.AdConst.AD_TEXT && jumpTag.contains(Const.AdConst.AD_TEXT).not()) {
                            jumpTag.add(Const.AdConst.AD_TEXT)
                            jumpLiveData.postValue(jumpTag)
                        }
                        return
                    }
                    loadAdInstancePlace(adWrapper, place, index + 1)
                }

                override fun onShow() {
                    Log.d(TAG,"$place 展示成功 id:${adWrapper.id}, 权重:${adWrapper.weight} hashCode:${adWrapper.getAdInstance().hashCode()}")
                    clearNativeCallBack()
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
                        navPermission = false
                        Repository.sharedPreferences.edit { putBoolean(Const.AdConst.permissionNav, navPermission) }
                    }

                }
            })
        }.loadAd(adWrapper.id)

    }

    private fun loadIntAdInstance(index: Int,place: String, adWrapper: AdWrapper) {
        IntAd().apply {
            setAdCallBack(object : AdCallBack {
                override fun onLoadSuccess(adInstance: Any) {
                    fillEvent(adWrapper,place)

                    adWrapper.setAdInstance(adInstance)
                    fullAdPool.add(adWrapper)
                    Log.d(TAG,"$place 请求成功 全屏广告缓存池里数量:${fullAdPool.size}, 权重:${adWrapper.weight}, index:${index},hashCode:" + adInstance.hashCode())
                    if (place == Const.AdConst.AD_START && jumpTag.contains(Const.AdConst.AD_START).not()) {
                        jumpTag.add(Const.AdConst.AD_START)
                        jumpLiveData.postValue(jumpTag)
                    }
                }

                override fun onLoadFail(code: String, msg: String) {
                    Log.d(TAG,"$place 请求失败 msg:$msg id:${adWrapper.id} 权重:${adWrapper.weight} ,index:${index} ")
                    adWrapper.adLoading = false
                    if (index > adWrapper.innerAdList.size - 2) {
                        if (place == Const.AdConst.AD_START && jumpTag.contains(Const.AdConst.AD_START)
                                .not()
                        ) {
                            jumpTag.add(Const.AdConst.AD_START)
                            jumpLiveData.postValue(jumpTag)
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
                    intAdCallBack?.onCloseIntAd()
                }

                override fun onClick() {
                    clickEvent(adWrapper,place)
                }
            })
        }.loadAd(adWrapper.id)
    }

    private fun loadOpenAdInstance(index: Int, place: String ,adWrapper: AdWrapper) {
        OpenAd().apply {
            setAdCallBack(object :AdCallBack{
                override fun onLoadSuccess(adInstance: Any) {
                    fillEvent(adWrapper,place)

                    adWrapper.setAdInstance(adInstance)
                    fullAdPool.add(adWrapper)
                    Log.d(TAG,"$place 请求成功 全屏广告缓存池里数量:${fullAdPool.size}, 权重:${adWrapper.weight}, index:${index},hashCode:" + adInstance.hashCode())

                    if (place == Const.AdConst.AD_START && jumpTag.contains(Const.AdConst.AD_START).not()) {
                        jumpTag.add(Const.AdConst.AD_START)
                        jumpLiveData.postValue(jumpTag)
                    }
                }

                override fun onLoadFail(code: String, msg: String) {
                    Log.d(TAG,"$place 请求失败 msg:$msg id:${adWrapper.id} 权重:${adWrapper.weight} ,index:${index} ")
                    adWrapper.adLoading = false
                    if (index > adWrapper.innerAdList.size - 2) {
                        if (place == Const.AdConst.AD_START && jumpTag.contains(Const.AdConst.AD_START).not()) {
                            jumpTag.add(Const.AdConst.AD_START)
                            jumpLiveData.postValue(jumpTag)
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
                    intAdCallBack?.onCloseIntAd()
                }

                override fun onClick() {
                    clickEvent(adWrapper,place)
                }
            })
        }.loadAd(adWrapper.id)
    }

    fun getAdObjFromPool(adLocation: String) {
        try {
            if ((!::mAdConfigMap.isInitialized)) {
                if (adLocation == Const.AdConst.AD_START || adLocation == Const.AdConst.AD_INSERT) {
                    intAdCallBack?.getIntAdFromPool(null)
                }
                return
            }

            if (mAdConfigMap[adLocation]?.openBtn == false) {
                if (adLocation == Const.AdConst.AD_START || adLocation == Const.AdConst.AD_INSERT) {
                    intAdCallBack?.getIntAdFromPool(null)
                }
                return
            }

            if (App.isBackground) {
                Log.d(TAG, "禁止在后台获取广告 ${adLocation}")
                if (adLocation == Const.AdConst.AD_START || adLocation == Const.AdConst.AD_INSERT) {
                    intAdCallBack?.getIntAdFromPool(null)
                }
                return
            }

            val isSmallAdConst = adLocation in listOf(Const.AdConst.AD_TEXT, Const.AdConst.AD_OTHER,Const.AdConst.AD_INITIAL)
            val pool = if (!isSmallAdConst) fullAdPool else smallAdPool
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
                val sortList = fullAdPool.sortedByDescending { it.weight }
                if (sortList.isEmpty()) {
                    intAdCallBack?.getIntAdFromPool(null)
                    return
                }
                val lowAdWeight = sortList.last().weight
                sortList.forEach {
                    if (adLocation != Const.AdConst.AD_START && it.type == Const.AdConst.TYPE_OPEN) {
                        return@forEach
                    }

                    if (sortList.size == 1) {
                        intAdCallBack?.getIntAdFromPool(it)
                        fullAdPool.remove(it)
                        return
                    }

                    if (it.weight > lowAdWeight) {
                        intAdCallBack?.getIntAdFromPool(it)
                        fullAdPool.remove(it)
                        return
                    }
                }

                val adWrapper: AdWrapper? = fullAdPool.find { it.place == adLocation }
                adWrapper?.let {
                    intAdCallBack?.getIntAdFromPool(it)
                    fullAdPool.remove(it)
                    return
                }
                intAdCallBack?.getIntAdFromPool(null)
            } else {
                val sortList = smallAdPool.sortedByDescending { it.weight }
                val lowAdWeight = sortList.last().weight
                sortList.forEach {
                    if (it.weight > lowAdWeight) {
                        smallAdPool.remove(it)
                        nativeAdCallMap[adLocation]?.getNavAdFromPool(it)
                        return
                    }
                }

                val adWrapper: AdWrapper? = smallAdPool.find { it.place == adLocation }
                adWrapper?.let {
                    smallAdPool.remove(it)
                    nativeAdCallMap[adLocation]?.getNavAdFromPool(it)
                    return
                }
                //本广告位请求中，缓存广告权重相同时情况
                if (mAdConfigMap[adLocation]?.openBtn == true) {
                    smallAdPool.first().let {
                        smallAdPool.remove(it)
                        nativeAdCallMap[adLocation]?.getNavAdFromPool(it)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "获取广告:" + e.message + " ,广告位:" + adLocation)
        }
    }

    private fun clearJumpTag() {
        jumpTag.clear()
        if (fullAdPool.any { it.place == Const.AdConst.AD_START}){
            jumpTag.add(Const.AdConst.AD_START)
        }

        if (smallAdPool.any { it.place == Const.AdConst.AD_TEXT }){
            jumpTag.add(Const.AdConst.AD_TEXT)
        }
    }

    fun loadingAdPool() {
        Log.d(TAG, "-----------------> 检查广告池 <-----------------")
        clearJumpTag()
        loadAdInstance(Const.AdConst.AD_START, Const.AdConst.AD_INSERT, Const.AdConst.AD_INITIAL,Const.AdConst.AD_OTHER, Const.AdConst.AD_TEXT)
    }
}

fun getMoringTime(): Long {
    val times = System.currentTimeMillis()
    return times - (times + TimeZone.getDefault().rawOffset) % (24 * 60 * 60 * 1000L)
}