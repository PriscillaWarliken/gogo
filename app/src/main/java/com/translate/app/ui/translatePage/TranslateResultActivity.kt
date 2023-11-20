package com.translate.app.ui.translatePage

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.gms.ads.nativead.NativeAd
import com.google.gson.JsonArray
import com.nguyenhoanglam.imagepicker.model.CustomColor
import com.nguyenhoanglam.imagepicker.model.CustomMessage
import com.nguyenhoanglam.imagepicker.model.GridCount
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.model.ImagePickerConfig
import com.nguyenhoanglam.imagepicker.model.IndicatorType
import com.nguyenhoanglam.imagepicker.model.RootDirectory
import com.nguyenhoanglam.imagepicker.ui.imagepicker.registerImagePicker
import com.translate.app.App
import com.translate.app.Const
import com.translate.app.R
import com.translate.app.ads.AdManager
import com.translate.app.ads.base.AdWrapper
import com.translate.app.ads.callback.IntAdCallback
import com.translate.app.ads.callback.NavAdCallback
import com.translate.app.repository.Repository
import com.translate.app.ui.BaseActivity
import com.translate.app.ui.ImagePickerActivity
import com.translate.app.ui.TopBar
import com.translate.app.ui.languagePage.LanguageActivity
import com.translate.app.ui.languagePage.LanguageChangeListener
import com.translate.app.ui.ocrPage.CaptureActivity
import com.translate.app.ui.ocrPage.OCRActivity
import com.translate.app.ui.pointLog
import com.translate.app.ui.weight.CoilImage
import com.translate.app.ui.weight.NativeAdsView
import com.translate.app.ui.weight.RateDialog
import com.translate.app.ui.weight.SmallNavView
import com.translate.app.ui.weight.click
import kotlinx.coroutines.launch


class TranslateResultActivity : BaseActivity(),LanguageChangeListener, NavAdCallback,IntAdCallback {

    val viewModel by viewModels<TranslateViewModel>()
    private var showAnimState by mutableStateOf(value = false)
    private var showDialog by mutableStateOf(value = false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Repository.sharedPreferences.apply {
            var count = getInt(Const.RESULT_COUNT,0)
            edit {
                putInt(Const.RESULT_COUNT,++count)
            }
        }
        pointLog("Textresults_And","文本翻译结果页曝光")
        TranslateViewModel.reusltLiveData.observe(this){
            showAnimState = false
        }
        if (Repository.sharedPreferences.getInt(Const.RESULT_COUNT, 0) == 2 && Repository.sharedPreferences.getBoolean(Const.SHOW_RATE,true)) {
            Repository.sharedPreferences.edit {
                putBoolean(Const.SHOW_RATE,false)
            }
            showDialog = true
        }
        setContent {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TopBar{
                    execApi()
                }

                Box(modifier = Modifier.fillMaxWidth().height(92.dp)){
                    if (adWrapper.value == null) {
                        SmallNavView(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .padding(horizontal = 20.dp)
                        )
                    }else{
                        NativeAdsView(
                            isBig = false, mAdInstance = adWrapper.value!!, modifier = Modifier
                                .padding(top = 20.dp)
                                .padding(horizontal = 20.dp)
                        )
                    }
                }


                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(0.95f)
                        .background(color = Color(0xFF4974C9), shape = RoundedCornerShape(24.dp))
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = TranslateViewModel.srcText,
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 125.dp)
                    )

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .height(1.dp)
                            .background(color = Color(0x80FFFFFF))
                    )

                    Text(
                        text = TranslateViewModel.dstText,
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 149.dp)
                    )

                    CoilImage(
                        modifier = Modifier
                            .padding(bottom = 16.dp, end = 16.dp)
                            .align(Alignment.End)
                            .size(38.dp)
                            .click {
                                copyText(TranslateViewModel.dstText)

                            }, data = R.drawable.home_copy
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Box {
                        CoilImage(
                            modifier = Modifier
                                .size(155.dp, 128.dp)
                                .click {
                                    start()
                                }, data = R.drawable.home_album
                        )
                        Text(
                            text = "Album", fontSize = 18.sp, modifier = Modifier
                                .padding(bottom = 16.dp)
                                .align(Alignment.BottomCenter)
                        )
                    }
                    Box {
                        CoilImage(
                            modifier = Modifier
                                .size(155.dp, 128.dp)
                                .click {
                                    navActivity<CaptureActivity>()
                                }, data = R.drawable.home_camera
                        )
                        Text(
                            text = "Camera",
                            fontSize = 18.sp,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .align(Alignment.BottomCenter)
                        )
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
                        .align(Alignment.Center)) {
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            contentScale = ContentScale.None
                        )
                        Text(text = "Translating...", fontSize = 24.sp,color = Color.White)
                    }
                }
            }
            Dialogs()
        }
    }

    @Composable
    private fun Dialogs() {
        if (showDialog){
            RateDialog {
                showDialog = false
            }
        }
    }

    var isBackground = false
    override fun onStart() {
        super.onStart()
        isBackground = App.isBackground
    }

    override fun onResume() {
        super.onResume()
        if (isBackground.not()) {
            AdManager.setNativeCallBack(this, Const.AdConst.AD_TEXT)
            AdManager.getAdObjFromPool(Const.AdConst.AD_TEXT)
        }
        LanguageActivity.setLanguageChangeListener(this)
    }

    override fun getNavAdFromPool(adWrapper: AdWrapper) {
        this.adWrapper.value=adWrapper.getAdInstance() as NativeAd
    }

    private fun copyText(text: String?) {
        val clipboardManager = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this@TranslateResultActivity, "Copied !", Toast.LENGTH_SHORT).show()
    }

    override fun changeLanguage(sourceLanguage: String, targetLanguage: String) {
        execApi()
    }

    private fun execApi() {
        showAnimState = true
        val arr = JsonArray().apply {
            add(TranslateViewModel.srcText)
        }
        lifecycleScope.launch {
            viewModel.execTranslateApi(
                arr,
                Repository.sourceLanguage!!.language,
                Repository.targetLanguage!!.language
            )
            showIntAd()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun showIntAd() {
        AdManager.setIntAdCallBack(this)
        AdManager.getAdObjFromPool(Const.AdConst.AD_INSERT)
    }

    override fun getIntAdFromPool(adWrapper: AdWrapper?) {
        adWrapper?.let { it.showAdInstance(this) }
        AdManager.clearIntAdCallBack()
    }

    override fun onCloseIntAd() {}

}