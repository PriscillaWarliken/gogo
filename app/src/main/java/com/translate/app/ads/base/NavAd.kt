package com.translate.app.ads.base

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.translate.app.App
import com.translate.app.R

class NavAd:BaseAd() {
    override fun loadAd(adId: String) {
        val loader = AdLoader.Builder(App.context, adId)
            .forNativeAd { nativeAd ->
                getMyAdCallBack().onLoadSuccess(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    getMyAdCallBack().onClick()
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    getMyAdCallBack().onLoadFail(p0.code.toString(), p0.message)
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    getMyAdCallBack().onShow()
                }
            })
        loader.build().loadAd(AdRequest.Builder().build())
    }


    companion object {

        fun fitSmallNavMaterial(context: Context, viewGroup: ViewGroup, nativeAd: NativeAd) {
            try {
                val inflater =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val adView = inflater.inflate(R.layout.nav_layout, null) as NativeAdView
                val adTitle = adView.findViewById<TextView>(R.id.ad_title)
                val adContent = adView.findViewById<TextView>(R.id.ad_content)
                val adIcon = adView.findViewById<ImageView>(R.id.ad_icon)
                val install = adView.findViewById<TextView>(R.id.ad_call)

                adTitle.text = nativeAd.headline
                adContent.text = nativeAd.body
                adIcon.setImageDrawable(nativeAd.icon!!.drawable)
                install.text = nativeAd.callToAction

                adView.headlineView = adTitle
                adView.bodyView = adContent
                adView.iconView = adIcon
                adView.callToActionView = install
                adView.setNativeAd(nativeAd)
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
            } catch (_: Exception) {
            }
        }

        fun fillNavMaterial(context: Context, viewGroup: ViewGroup, nativeAd: NativeAd) {
            try {
                val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val adView = inflater.inflate(R.layout.nav_layout_big, null) as NativeAdView
                val adTitle = adView.findViewById<TextView>(R.id.ad_title)
                val adContent = adView.findViewById<TextView>(R.id.ad_content)
                val adIcon = adView.findViewById<ImageView>(R.id.ad_icon)
                val install = adView.findViewById<TextView>(R.id.ad_call)
                adView.mediaView = adView.findViewById(R.id.media_view)

                adTitle.text = nativeAd.headline
                adContent.text = nativeAd.body
                adIcon.setImageDrawable(nativeAd.icon!!.drawable)
                install.text = nativeAd.callToAction

                adView.headlineView = adTitle
                adView.bodyView = adContent
                adView.iconView = adIcon
                adView.callToActionView = install
                adView.setNativeAd(nativeAd)
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
            } catch (_: Exception) { }
        }
    }
}
