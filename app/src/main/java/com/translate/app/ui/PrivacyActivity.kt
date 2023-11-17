package com.translate.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.google.firebase.analytics.logEvent
import com.translate.app.App
import com.translate.app.Const
import com.translate.app.R
import com.translate.app.repository.Repository
import com.translate.app.ui.weight.CoilImage
import com.translate.app.ui.weight.click

class PrivacyActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pointLog("Privacy_And","隐私征求页曝光")
        setContent {
            BackHandler(enabled = true) {}
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CoilImage(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 47.dp, start = 42.dp)
                        .size(230.dp, 38.dp),
                    data = R.mipmap.welcome
                )
                Text(
                    text = stringResource(id = R.string.app_name),
                    fontSize = 24.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 15.dp, start = 42.dp)
                )
                CoilImage(
                    modifier = Modifier
                        .padding(top = 45.dp)
                        .size(308.dp, 318.dp),
                    data = R.mipmap.inbetweening
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF333333), fontSize = 14.sp)){
                            append("Click \"Start\" to start the experience\nand accept our")
                        }
                        withStyle(SpanStyle(color = Color(0xFF58B9EE), fontSize = 14.sp)){
                            append(" Privacy Policy")
                        }
                    },
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(top = 40.dp)
                        .click {
                            navActivity<WebActivity>()
                        },
                    textAlign = TextAlign.Center,
                )

                Button(
                    onClick = {
                        pointLog("Start_And","隐私征求页点击Start")
                        Repository.sharedPreferences.edit {
                            putBoolean(Const.PRIVACY_AGREE,false)
                        }
                        navActivity<StartActivity>()
                        finish()
                    }, modifier = Modifier
                        .padding(top = 40.dp)
                        .size(310.dp, 60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6ACAFF))
                ) {
                    Text(text = "Start",color = Color.White, fontSize = 24.sp,modifier = Modifier)
                }
            }
        }
    }

}

fun pointLog(pointEvent:String,des:String) {
    App.firebaseAnalytics.logEvent(pointEvent){}
    Log.d("pointLog", des)
}