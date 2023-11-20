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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
    private var clickTag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pointLog("NoText_And","文本输入 无 数据曝光")
        setContent {
            BackHandler(enabled = true) {
                if (Repository.extraAd_button.not()) {
                    finish()
                    return@BackHandler
                }
                clickTag = 2
                showIntAd()
            }
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
                    TopBar(finishBlock = {
                        if (Repository.extraAd_button.not()) {
                            finish()
                            return@TopBar
                        }
                        clickTag = 2
                        showIntAd()
                    })

                    TranslateEditView(
                        text = "",
                        hintText = "Enter text...",
                        modifier = Modifier
                            .size(355.dp, 414.dp)
                            .padding(start = 12.dp, end = 12.dp, top = 20.dp)
                            .background(
                                color = Color(0xFF4974C9),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        onNext = { result->
                            focusManager.clearFocus()
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
                                clickTag = 1
                                showIntAd()
                            }
                        }
                    )

                    MyNativeView()
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

    @Composable
    private fun MyNativeView() {
        adWrapper.value?.let {
            NativeAdsView(
                mAdInstance = it, modifier = Modifier
                    .padding(top = 20.dp)
                    .padding(horizontal = 20.dp)
                    .shadow(elevation = 1.dp, ambientColor = Color.Black)
            )
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
        if (clickTag == 1) {
            navActivity<TranslateResultActivity>()
        }else{
            finish()
        }

    }

    override fun onCloseIntAd() {
        if (clickTag == 1) {
            navActivity<TranslateResultActivity>()
        }else{
            finish()
        }
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