package com.translate.app.ui

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.nativead.NativeAd
import com.translate.app.App
import com.translate.app.Const
import com.translate.app.R
import com.translate.app.ads.AdManager
import com.translate.app.ads.base.AdWrapper
import com.translate.app.ads.callback.IntAdCallback
import com.translate.app.ads.callback.NavAdCallback
import com.translate.app.repository.Repository
import com.translate.app.ui.weight.CoilImage
import com.translate.app.ui.weight.NativeAdsView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StartActivity : BaseActivity(), IntAdCallback,NavAdCallback {

    private var launchTime = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Repository.sharedPreferences.edit { putLong(Const.USE_APP_TIME,System.currentTimeMillis()) }
        if (Repository.sharedPreferences.getBoolean(Const.PRIVACY_AGREE,true)) {
            navActivity<PrivacyActivity>()
            return
        }

        if (Repository.sharedPreferences.getBoolean(Const.START_EXPORT, true)) {
            pointLog("Open_And","启动页曝光（总的）")
            Repository.sharedPreferences.edit { putBoolean(Const.START_EXPORT,false) }
        }

        setContent {
            BackHandler(enabled = true) {}
            Box(modifier = Modifier.fillMaxSize()){
                adWrapper.value?.let {
                    NativeAdsView(
                        isBig = false,
                        mAdInstance = it, modifier = Modifier
                            .padding(top = 40.dp)
                            .padding(horizontal = 20.dp)
                            .align(Alignment.TopCenter)
                    )
                }
                CoilImage(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(290.dp),
                    data = R.mipmap.start
                )
                ProgressView(modifier = Modifier
                    .padding(bottom = 130.dp)
                    .align(Alignment.BottomCenter), launchTime)
                {
                    showOpenAd()
                }

            }
        }


        try {
            if (AdManager.adMapLiveData.hasActiveObservers().not()) {
                AdManager.adMapLiveData.observe(this) {
                    if (it) {
                        AdManager.loadingAdPool()
                    }
                }
            }
            if (AdManager.skipLiveData.hasActiveObservers().not()) {
                AdManager.skipLiveData.observe(this){
                    if (it.contains(Const.AdConst.AD_TEXT) && it.contains(Const.AdConst.AD_START)) {
                        launchTime = 1
                    }
                }
            }
        }catch (_:Exception){}
    }

    override fun onStart() {
        super.onStart()
        if (App.isBackground.not()) {
            AdManager.setNativeCallBack(this, Const.AdConst.AD_INITIAL)
            AdManager.getAdObjFromPool(Const.AdConst.AD_INITIAL)
        }
        AdManager.clearNativeCallBack()
    }


    override fun getNavAdFromPool(adWrapper: AdWrapper) {
        this.adWrapper.value = adWrapper.getAdInstance() as NativeAd
    }

    override fun onStop() {
        super.onStop()
        AdManager.skipLiveData.removeObservers(this)
        AdManager.adMapLiveData.removeObservers(this)
    }

    private fun showOpenAd() {
        AdManager.setIntAdCallBack(this)
        AdManager.getAdObjFromPool(Const.AdConst.AD_START)
    }

    override fun getIntAdFromPool(adWrapper: AdWrapper?) {
        adWrapper?.let {
            it.showAdInstance(this)
            return
        }
        navActivity<MainActivity>()
        finish()
    }

    override fun onCloseIntAd() {
        navActivity<MainActivity>()
        finish()
    }

    @Composable
    private fun ProgressView(modifier:Modifier,duration:Int,block:()->Unit) {
        var progressLinear by remember {
            mutableStateOf(0.00f)
        }

        LaunchedEffect(Unit){
            for (i in 0..100) {
                progressLinear += 0.01f
                delay(launchTime * 10L)
            }
            block.invoke()
        }

        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = progressLinear,
                trackColor = Color(0xFFEEEFEF),
                color = Color(0xFF6ACAFF),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(15.dp)
                    .clip(shape = RoundedCornerShape(90.dp))
            )
        }
    }
}




