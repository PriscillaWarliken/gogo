package com.translate.app.ads.callback

import com.translate.app.ads.base.AdWrapper

interface NavAdCallback {
    fun getNavAdFromPool(adWrapper: AdWrapper)
}