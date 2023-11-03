package com.translate.app.ads.base

import com.translate.app.ads.callback.AdCallBack

abstract class BaseAd {
    private lateinit var adCallBack: AdCallBack

    abstract fun loadAd(adId: String)

    fun setMyAdCallBack(aaa: AdCallBack) {
        this.adCallBack = aaa
    }

    fun getMyAdCallBack(): AdCallBack {
        return adCallBack
    }
}