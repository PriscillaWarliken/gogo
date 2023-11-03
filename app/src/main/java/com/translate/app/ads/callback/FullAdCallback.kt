package com.translate.app.ads.callback

import com.translate.app.ads.base.AdWrapper

interface FullAdCallback {
    fun getFullFromPool(adWrapper: AdWrapper?)

    fun onCloseFull()
}