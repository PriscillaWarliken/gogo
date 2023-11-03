package com.translate.app.ui.weight

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.translate.app.ads.base.AdWrapper
import kotlinx.coroutines.launch

@Composable
fun NativeAdsView(isBig:Boolean = true,adWrapper: AdWrapper, modifier: Modifier) {
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
                    adWrapper.showAdInstance(context,it,isBig)
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