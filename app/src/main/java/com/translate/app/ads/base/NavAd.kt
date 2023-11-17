package com.translate.app.ads.base

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
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
                getAdCallBack().onLoadSuccess(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    getAdCallBack().onClick()
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    getAdCallBack().onLoadFail(p0.code.toString(), p0.message)
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    getAdCallBack().onShow()
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
                adView.findViewById<TextView>(R.id.tag_tv).visibility = View.VISIBLE

                adTitle.text = nativeAd.headline
                adContent.text = nativeAd.body
                nativeAd.icon?.let {
                    adIcon.setImageDrawable(it.drawable)
                    adIcon.setBackgroundColor(Color.TRANSPARENT)
                }

                install.text = nativeAd.callToAction

                adView.headlineView = adTitle
                adView.bodyView = adContent
                adView.iconView = adIcon
                adView.callToActionView = install

                adTitle.setBackgroundColor(Color.TRANSPARENT)
                adContent.setBackgroundColor(Color.TRANSPARENT)
                install.setBackgroundResource(R.drawable.ad_install)
                adView.setNativeAd(nativeAd)
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
            } catch (e: Exception) {
                e.printStackTrace()
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
                adView.findViewById<TextView>(R.id.tag_tv).visibility = View.VISIBLE


                adTitle.text = nativeAd.headline
                adContent.text = nativeAd.body
                nativeAd.icon?.let {
                    adIcon.setImageDrawable(it.drawable)
                    adIcon.setBackgroundColor(Color.TRANSPARENT)
                }
                install.text = nativeAd.callToAction

                adView.headlineView = adTitle
                adView.bodyView = adContent
                adView.iconView = adIcon
                adView.callToActionView = install

                adTitle.setBackgroundColor(Color.TRANSPARENT)
                adContent.setBackgroundColor(Color.TRANSPARENT)
                install.setBackgroundResource(R.drawable.ad_install)
                adView.setNativeAd(nativeAd)
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
