package com.translate.app.ads.base

import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.translate.app.App

class IntAd:BaseAd() {
    override fun loadAd(adId: String) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(App.context,adId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                getMyAdCallBack().onLoadFail(adError.code.toString(),adError.message)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                setFullCallBack(interstitialAd)
                getMyAdCallBack().onLoadSuccess(interstitialAd)
            }
        })
    }

    private fun setFullCallBack(interstitialAd: InterstitialAd) {
        interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                getMyAdCallBack().onClose()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d("AdLog", "插页展示失败 hashCode:${interstitialAd.hashCode()}")
                getMyAdCallBack().onClose()
            }

            override fun onAdShowedFullScreenContent() {
                getMyAdCallBack().onShow()
            }
        }
    }
}