package com.translate.app.ads.base

import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.appopen.AppOpenAd
import com.translate.app.App

class OpenAd:BaseAd() {
    override fun loadAd(adId: String) {
        val request = AdManagerAdRequest.Builder().build()
        AppOpenAd.load(
            App.context, adId, request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdLoaded(ad: AppOpenAd) {
                    setFullScreenCallBack(ad)
                    getMyAdCallBack().onLoadSuccess(ad)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    getMyAdCallBack().onLoadFail(loadAdError.code.toString(),loadAdError.message)
                }
            }
        )
    }

    private fun setFullScreenCallBack(ad: AppOpenAd) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                getMyAdCallBack().onClose()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d("AdLog", "开屏展示失败 hashCode:${ad.hashCode()}")
                getMyAdCallBack().onClose()
            }

            override fun onAdShowedFullScreenContent() {
                getMyAdCallBack().onShow()
            }
        }
    }
}