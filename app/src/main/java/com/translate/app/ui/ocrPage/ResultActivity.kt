package com.translate.app.ui.ocrPage

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.translate.app.Const
import com.translate.app.R
import com.translate.app.repository.Repository
import com.translate.app.ui.BaseActivity
import com.translate.app.ui.weight.CoilImage
import com.translate.app.ui.weight.click

class ResultActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(modifier = Modifier.fillMaxSize()){
                OCRActivity.resultBitmap?.let{
                    PreViewImageLayout(
                        it, modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(0.83f)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                            )
                    )
                }

                Row(modifier = Modifier
                    .padding(bottom = 47.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Box {
                        CoilImage(modifier = Modifier
                            .size(155.dp, 128.dp)
                            .click {

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
            }
        }
    }
}