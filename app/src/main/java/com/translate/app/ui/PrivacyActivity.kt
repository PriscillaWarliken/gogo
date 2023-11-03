package com.translate.app.ui

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.translate.app.Const
import com.translate.app.R
import com.translate.app.repository.Repository
import com.translate.app.ui.weight.CoilImage
import com.translate.app.ui.weight.click

class PrivacyActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
                    text = "Click \"Start\" to start the experience\nand accept our Privacy Policy",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(top = 40.dp),
                    textAlign = TextAlign.Center,
                )

                Box(
                    modifier = Modifier
                        .click {
                            Repository.sharedPreferences.edit {
                                putBoolean(Const.PRIVACY_AGREE,false)
                            }
                            navActivity<StartActivity>()
                            finish()
                        }
                        .padding(top = 40.dp)
                        .size(310.dp, 60.dp)
                        .background(color = Color(0xFF6ACAFF), shape = RoundedCornerShape(12.dp))
                ) {
                    Text(text = "Start",color = Color.White, fontSize = 24.sp,modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
    
    
    
}