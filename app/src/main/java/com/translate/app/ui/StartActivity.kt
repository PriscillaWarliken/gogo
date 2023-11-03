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
import com.translate.app.Const
import com.translate.app.R
import com.translate.app.ads.AdManager
import com.translate.app.ads.base.AdWrapper
import com.translate.app.ads.callback.FullAdCallback
import com.translate.app.ads.getTodayTime
import com.translate.app.repository.Repository
import com.translate.app.ui.weight.CoilImage
import kotlinx.coroutines.delay

class StartActivity : BaseActivity(), FullAdCallback {

    private var launchTime = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Repository.sharedPreferences.getLong(Const.USE_APP_TIME, 0L) < getTodayTime()) {
            //重置今天使用翻译次数次数
            Repository.sharedPreferences.edit {
                putInt(Const.TRANSLATE_COUNT,0)
            }
        }

        Repository.sharedPreferences.edit { putLong(Const.USE_APP_TIME,System.currentTimeMillis()) }
        if (Repository.sharedPreferences.getBoolean(Const.PRIVACY_AGREE,true)) {
            navActivity<PrivacyActivity>()
            return
        }

        setContent {
            BackHandler(enabled = true) {}
            Box(modifier = Modifier.fillMaxSize()){
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

        AdManager.loadingAdPool()
        try {
            if (AdManager.skipLiveData.hasActiveObservers()) {
                return
            }
            AdManager.skipLiveData.observe(this){
                if (it.contains(Const.AdConst.AD_TEXT) && it.contains(Const.AdConst.AD_START)) {
                    launchTime = 1
                }
            }
        }catch (_:Exception){}
    }

    private fun showOpenAd() {
        if (true) {
            AdManager.setFullCallBack(this)
            AdManager.getAdInstanceFromPool(Const.AdConst.AD_START)
        }else{
            navActivity<MainActivity>()
            finish()
        }
    }

    override fun getFullFromPool(adWrapper: AdWrapper?) {
        adWrapper?.let {
            it.showAdInstance(this)
            return
        }
        navActivity<MainActivity>()
        finish()
    }

    override fun onCloseFull() {
        navActivity<MainActivity>()
        finish()
    }
}

@Composable
private fun ProgressView(modifier:Modifier,duration:Int,block:()->Unit) {
    var progressLinear by remember {
        mutableStateOf(0.00f)
    }

    LaunchedEffect(Unit){
        for (i in 0..100) {
            progressLinear += 0.01f
            delay(duration * 10L)
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


