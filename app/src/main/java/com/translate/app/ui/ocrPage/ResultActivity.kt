package com.translate.app.ui.ocrPage

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
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
import com.google.android.gms.ads.nativead.NativeAd
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
import com.translate.app.ads.callback.NavAdCallback
import com.translate.app.repository.Repository
import com.translate.app.ui.BaseActivity
import com.translate.app.ui.ImagePickerActivity
import com.translate.app.ui.MainActivity
import com.translate.app.ui.languagePage.LanguageActivity
import com.translate.app.ui.pointLog
import com.translate.app.ui.translatePage.TranslateActivity
import com.translate.app.ui.weight.CoilImage
import com.translate.app.ui.weight.NativeAdsView
import com.translate.app.ui.weight.RateDialog
import com.translate.app.ui.weight.SmallNavView
import com.translate.app.ui.weight.click
import java.util.Calendar


class ResultActivity : BaseActivity(),NavAdCallback {
    private var showDialog by mutableStateOf(value = false)

    companion object{
        var fromCamera:Boolean = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (fromCamera) {
            pointLog("Cameraresults_And","拍照翻译结果页曝光")
        }else{
            pointLog("Albumresults _And","照片翻译结果页曝光")
        }
        Repository.sharedPreferences.apply {
            var count = getInt(Const.RESULT_COUNT,0)
            edit {
                putInt(Const.RESULT_COUNT,++count)
            }
        }
        if (Repository.sharedPreferences.getInt(Const.RESULT_COUNT, 0) == 2 && Repository.sharedPreferences.getBoolean(Const.SHOW_RATE,true)) {
            Repository.sharedPreferences.edit {
                putBoolean(Const.SHOW_RATE,false)
            }
            showDialog = true
        }
        setContent {
            BackHandler(enabled = true) {
                if (fromCamera) {
                    finish()
                }else{
                    start()
                }
            }
            Box(modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                ){

                Column(
                    modifier = Modifier
                        .padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CoilImage(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .size(24.dp)
                            .click {
                                if (fromCamera) {
                                    finish()
                                } else {
                                    start()
                                }

                            },
                        data = R.mipmap.universal_back
                    )

                    MyNativeViewPlace()

                    OCRActivity.resultBitmap?.let{
                        val scrollState = rememberScrollState()
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                            )
                            .verticalScroll(scrollState),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                            PreViewImageLayout(
                                it, modifier = Modifier,
                                shareView = {
                                    Row(
                                        Modifier
                                            .padding(bottom = 22.dp)
                                            .size(138.dp, 48.dp)
                                            .background(
                                                color = Color(0x4D000000),
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        CoilImage(modifier = Modifier
                                            .size(22.dp)
                                            .click {
                                                shareBitmap()
                                            }, data = R.mipmap.share)
                                        CoilImage(modifier = Modifier
                                            .size(22.dp)
                                            .click {
                                                copyText()
                                            }, data = R.mipmap.copy)
                                    }
                                })
                            Row(modifier = Modifier
                                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                Box {
                                    CoilImage(modifier = Modifier
                                        .size(155.dp, 128.dp)
                                        .click {
                                            navActivity<TranslateActivity>()
                                            finish()
                                            showInt()
                                        }, data = R.mipmap.text)
                                    Text(text = "Text", fontSize = 18.sp,color = Color.White,modifier = Modifier
                                        .padding(bottom = 16.dp)
                                        .align(Alignment.BottomCenter))
                                }
                                Box {
                                    CoilImage(
                                        modifier = Modifier
                                            .size(155.dp, 128.dp)
                                            .click {
                                                if (fromCamera) {
                                                    start()
                                                } else {
                                                    navActivity<CaptureActivity>()
                                                    finish()
                                                }
                                                showInt()
                                            }, data = if (fromCamera) {
                                            R.mipmap.home_album
                                        }else{
                                            R.mipmap.home_camera
                                        }
                                    )
                                    Text(text = if (fromCamera) "Album" else "Camera", fontSize = 18.sp,color = Color.White,modifier = Modifier
                                        .padding(bottom = 16.dp)
                                        .align(Alignment.BottomCenter))
                                }
                            }
                        }

                    }
                }

            }


            if (showDialog){
                RateDialog { showDialog = false }
            }
        }
    }

    @Composable
    private fun MyNativeViewPlace() {
        Box(modifier = Modifier.fillMaxWidth().height(92.dp)){
            if (adWrapper.value == null) {
                SmallNavView(modifier = Modifier.padding(top = 10.dp, start = 16.dp, end = 16.dp))
            } else {
                NativeAdsView(
                    isBig = false, mAdInstance = adWrapper.value!!, modifier = Modifier
                        .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                )
            }
        }
    }

    private fun copyText() {
        val clipboardManager =
            this@ResultActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", OCRActivity.resultStr)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this@ResultActivity, "Copied !", Toast.LENGTH_SHORT).show()
    }

    private fun shareBitmap() {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            val uri = Uri.parse(
                MediaStore.Images.Media.insertImage(
                    contentResolver,
                    OCRActivity.resultBitmap,
                    "IMG" + Calendar.getInstance().time, null
                )
            )
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(intent, "title"))
        }catch (e:Exception){}
    }


    override fun onStart() {
        super.onStart()
        if (App.isBackground.not() && canShowNav) {
            AdManager.setNativeCallBack(this, Const.AdConst.AD_OTHER)
            AdManager.getAdObjFromPool(Const.AdConst.AD_OTHER)
        }
    }

    override fun getNavAdFromPool(adWrapper: AdWrapper) {
        this.adWrapper.value=adWrapper.getAdInstance() as NativeAd
    }

}