package com.translate.app.ads.base

import com.translate.app.ads.callback.AdCallBack

abstract class BaseAd {
    private lateinit var adCallBack: AdCallBack

    abstract fun loadAd(adId: String)

    fun setAdCallBack(aaa: AdCallBack) {
        this.adCallBack = aaa
    }

    fun getAdCallBack(): AdCallBack {
        return adCallBack
    }
}