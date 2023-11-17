package com.translate.app.ui.weight

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.translate.app.R
import com.translate.app.ads.base.AdWrapper
import com.translate.app.ads.base.NavAd
import kotlinx.coroutines.launch

@Composable
fun BigNavView(modifier :Modifier ) {
    val context = LocalContext.current
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val adView = inflater.inflate(R.layout.nav_layout_big, null)
    Box(modifier = modifier){
        AndroidView(
            {
                adView
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp)
        )
    }
}

@Composable
fun SmallNavView(modifier :Modifier) {
    val context = LocalContext.current
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val adView = inflater.inflate(R.layout.nav_layout, null)
    Box(modifier = modifier){
        AndroidView(
            { adView },
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
fun NativeAdsView(isBig:Boolean = true,mAdInstance: NativeAd, modifier: Modifier) {
    val context = LocalContext.current as Activity
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val recompose = currentRecomposeScope
    var isFirst by remember {
        mutableStateOf(true)
    }
    val view=LocalView.current
    Box(modifier = modifier) {
        AndroidView(factory = {
            FrameLayout(it)
        }, update = {
            if (!isFirst) {
                scope.launch {
                    if (isBig) {
                        NavAd.fillNavMaterial(context,it, mAdInstance)
                    }else{
                        NavAd.fitSmallNavMaterial(context, it, mAdInstance)
                    }
                    view.post {
                        view.requestLayout()
                    }
                }
            } else {
                isFirst = false
            }
        }, modifier = Modifier
            .fillMaxWidth())
        LaunchedEffect(key1 = lifecycleOwner) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                recompose.invalidate()
            }
        }
    }
}