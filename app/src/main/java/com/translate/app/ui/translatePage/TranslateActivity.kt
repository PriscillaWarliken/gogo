package com.translate.app.ui.translatePage

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.gms.ads.nativead.NativeAd
import com.google.gson.JsonArray
import com.translate.app.App
import com.translate.app.Const
import com.translate.app.ads.AdManager
import com.translate.app.ads.base.AdWrapper
import com.translate.app.ads.callback.IntAdCallback
import com.translate.app.ads.callback.NavAdCallback
import com.translate.app.repository.Repository
import com.translate.app.ui.BaseActivity
import com.translate.app.ui.TopBar
import com.translate.app.ui.pointLog
import com.translate.app.ui.weight.NativeAdsView
import com.translate.app.ui.weight.TranslateEditView
import com.translate.app.ui.weight.click
import kotlinx.coroutines.launch

class TranslateActivity : BaseActivity(),IntAdCallback, NavAdCallback {

    val viewModel by viewModels<TranslateViewModel>()
    private var showAnimState by mutableStateOf(value = false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val focusManager = LocalFocusManager.current
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxSize()
            ){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .click {
                            focusManager.clearFocus()
                        },
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    TopBar()

                    TranslateEditView(
                        text = "",
                        hintText = "Enter text...",
                        modifier = Modifier
                            .size(355.dp, 414.dp)
                            .padding(start = 12.dp, end = 12.dp, top = 20.dp),
                        onNext = { result->
                            showAnimState = true
                            pointLog("Textanimation_And","文本翻译动效曝光")
                            val arr = JsonArray().apply {
                                add(result)
                            }
                            lifecycleScope.launch {
                                viewModel.execTranslateApi(
                                    arr,
                                    Repository.sourceLanguage!!.language,
                                    Repository.targetLanguage!!.language
                                )
                                showAnimState = false
                                showIntAd()
                            }
                        }
                    )

                    adWrapper.value?.let {
                        NativeAdsView(mAdInstance = it,modifier = Modifier
                            .padding(top = 20.dp)
                            .padding(horizontal = 20.dp))
                    }
                }
            }
            if (showAnimState) {
                BackHandler (enabled = true){}
                Box(modifier = Modifier
                    .click { }
                    .fillMaxSize()
                    .background(color = Color(0x4D000000))){
                    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("anim/文本翻译中.json"))
                    
                    Column(modifier = Modifier
                        .align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier,
                            contentScale = ContentScale.None
                        )
                        Text(text = "Translating...", fontSize = 24.sp,color = Color.White)
                    }
                }
            }
        }
    }

    private fun showIntAd() {
        AdManager.setIntAdCallBack(this)
        AdManager.getAdObjFromPool(Const.AdConst.AD_INSERT)
    }

    override fun getIntAdFromPool(adWrapper: AdWrapper?) {
        adWrapper?.let {
            it.showAdInstance(this)
            return
        }
        navActivity<TranslateResultActivity>()
    }

    override fun onCloseIntAd() {
        navActivity<TranslateResultActivity>()
    }

    override fun onStart() {
        super.onStart()
        if (App.isBackground.not()) {
            AdManager.setNativeCallBack(this, Const.AdConst.AD_TEXT)
            AdManager.getAdObjFromPool(Const.AdConst.AD_TEXT)
        }
    }

    override fun getNavAdFromPool(adWrapper: AdWrapper) {
        this.adWrapper.value = adWrapper.getAdInstance() as NativeAd
    }

}