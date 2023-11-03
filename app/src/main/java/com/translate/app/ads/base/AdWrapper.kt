package com.translate.app.ads.base

import android.app.Activity
import android.view.ViewGroup
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.translate.app.repository.bean.InnerAd

class AdWrapper(
    var place: String,
    var openBtn: Boolean,

    var innerAdList: List <InnerAd>,

    var adLoading: Boolean = false,
) {
    private lateinit var mAdInstance: Any
    lateinit var id: String
    lateinit var type: String
    var weight: Int = 0

    fun setAdInstance(ad: Any) {
        mAdInstance = ad
        adLoading = false
        setAdjustValueJob()
    }

    fun getAdInstance(): Any {
        return mAdInstance
    }

    fun showAdInstance(activity: Activity, viewGroup: ViewGroup? = null,isBig:Boolean = true) {
        when(mAdInstance) {
            is InterstitialAd -> {
                (mAdInstance as InterstitialAd).show(activity)
            }
            is NativeAd -> {
                viewGroup?.let {
                    if (isBig) {
                        NavAd.fillNavMaterial(activity,viewGroup, mAdInstance as NativeAd)
                    }else{
                        NavAd.fitSmallNavMaterial(activity, viewGroup, mAdInstance as NativeAd)
                    }
                }
            }
            is AppOpenAd -> {
                (mAdInstance as AppOpenAd).show(activity)
            }
        }
    }

    private fun setAdjustValueJob() {
        when (mAdInstance) {
            is InterstitialAd -> {
                (mAdInstance as InterstitialAd).setOnPaidEventListener {
                    uploadAdjustAdValue(it.valueMicros,it.currencyCode,place,id,"admob")
                }
            }
            is AppOpenAd -> {
                (mAdInstance as AppOpenAd).setOnPaidEventListener {
                    uploadAdjustAdValue(it.valueMicros,it.currencyCode,place,id,"admob")
                }
            }
            is NativeAd -> {
                (mAdInstance as NativeAd).setOnPaidEventListener {
                    uploadAdjustAdValue(it.valueMicros,it.currencyCode,place,id,"admob")
                }
            }
            is AdView -> {
                (mAdInstance as AdView).setOnPaidEventListener {
                    uploadAdjustAdValue(it.valueMicros,it.currencyCode,place,id,"admob")
                }
            }
        }
    }

    private fun uploadAdjustAdValue(valueMicros:Long, currencyCode: String, place: String, unity: String, plat: String) {
        //把原来的千分值转换成0.001
        val revenue = try {
            valueMicros/1000000.0
        } catch (e :Exception) {
            0.0
        }
        val adRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB).apply {
            setRevenue(revenue, currencyCode)
            //广告源渠道
            setAdRevenueNetwork(plat)
            //广告位名称
            setAdRevenuePlacement(place)
            //广告ID
            setAdRevenueUnit(unity)
        }
        //调用Adjust上报广告价值的方法
        Adjust.trackAdRevenue(adRevenue)
    }
}