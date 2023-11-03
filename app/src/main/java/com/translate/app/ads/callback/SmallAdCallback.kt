package com.translate.app.ads.callback

import com.translate.app.ads.base.AdWrapper

interface SmallAdCallback {
    fun getSmallFromPool(adWrapper: AdWrapper)
}