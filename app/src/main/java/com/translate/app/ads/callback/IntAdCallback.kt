package com.translate.app.ads.callback

import com.translate.app.ads.base.AdWrapper

interface IntAdCallback {
    fun getIntAdFromPool(adWrapper: AdWrapper?)

    fun onCloseIntAd()
}