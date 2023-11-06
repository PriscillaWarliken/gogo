package com.translate.app.ui.ocrPage

import android.R.attr.bitmap
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.translate.app.App
import com.translate.app.Const
import com.translate.app.R
import com.translate.app.ads.AdManager
import com.translate.app.ads.base.AdWrapper
import com.translate.app.ads.callback.SmallAdCallback
import com.translate.app.ui.BaseActivity
import com.translate.app.ui.MainActivity
import com.translate.app.ui.translatePage.TranslateActivity
import com.translate.app.ui.weight.CoilImage
import com.translate.app.ui.weight.NativeAdsView
import com.translate.app.ui.weight.click
import java.util.Calendar


class ResultActivity : BaseActivity(),SmallAdCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(modifier = Modifier
                .statusBarsPadding()
                .fillMaxSize()){

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp,end = 16.dp, top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CoilImage(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .size(24.dp)
                            .click {
                                navActivity<MainActivity>()
                            },
                        data = R.mipmap.universal_back
                    )

                    adWrapper.value?.let {
                        NativeAdsView(isBig = false, adWrapper = it,modifier = Modifier
                            .padding(top = 10.dp))
                    }
                }


                OCRActivity.resultBitmap?.let{
                    PreViewImageLayout(
                        it, modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(0.83f)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                            ),
                        bottomView = {
                            Row(modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                Box {
                                    CoilImage(modifier = Modifier
                                        .size(155.dp, 128.dp)
                                        .click {
                                            navActivity<TranslateActivity>()
                                            finish()
                                        }, data = R.mipmap.text)
                                    Text(text = "Text", fontSize = 18.sp,color = Color.White,modifier = Modifier
                                        .padding(bottom = 16.dp)
                                        .align(Alignment.BottomCenter))
                                }
                                Box {
                                    CoilImage(modifier = Modifier
                                        .size(155.dp, 128.dp)
                                        .click {
                                            navActivity<CaptureActivity>()
                                            finish()
                                        }, data = R.mipmap.home_camera
                                    )
                                    Text(text = "Camera", fontSize = 18.sp,color = Color.White,modifier = Modifier
                                        .padding(bottom = 16.dp)
                                        .align(Alignment.BottomCenter))
                                }
                            }
                        },
                        shareView = {
                            Row(
                                Modifier
                                    .padding(bottom = 22.dp)
                                    .align(Alignment.BottomCenter)
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
                                        val intent = Intent()
                                        intent.action = Intent.ACTION_SEND
                                        val uri = Uri.parse(MediaStore.Images.Media.insertImage(
                                            contentResolver,
                                            OCRActivity.resultBitmap,
                                            "IMG" + Calendar.getInstance().time, null)
                                        )
                                        intent.type = "image/*"
                                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                                        startActivity(Intent.createChooser(intent, "title"));
                                    }, data = R.mipmap.share)
                                CoilImage(modifier = Modifier
                                    .size(22.dp)
                                    .click {
                                        val clipboardManager =
                                            this@ResultActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clipData = ClipData.newPlainText("text", OCRActivity.resultStr)
                                        clipboardManager.setPrimaryClip(clipData)
                                        Toast.makeText(
                                            this@ResultActivity,
                                            "Copied !",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }, data = R.mipmap.copy)
                            }
                        })
                }


            }
        }
    }


    override fun onStart() {
        super.onStart()
        if (App.isBackground.not()) {
            AdManager.setSmallCallBack(this, Const.AdConst.AD_OTHER)
            AdManager.getAdObjFromPool(Const.AdConst.AD_OTHER)
        }
    }

    var adWrapper= mutableStateOf<AdWrapper?>(null)
    override fun getSmallFromPool(adWrapper: AdWrapper) {
        this.adWrapper.value=adWrapper
    }
}