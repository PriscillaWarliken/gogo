package com.translate.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.translate.app.R
import com.translate.app.ui.weight.CoilImage

open class BaseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    protected fun setContent(
        content:@Composable ()->Unit
    ) {
        setContent(parent = null,content= {
            val systemUiController = rememberSystemUiController()
            systemUiController.setStatusBarColor(
                color = Color.Transparent,
                darkIcons = true
            )
            systemUiController.setNavigationBarColor(
                color = Color.Transparent,
                darkIcons = true,
                navigationBarContrastEnforced = false
            )
            Box(modifier = Modifier.fillMaxSize()){
                CoilImage(modifier = Modifier.fillMaxSize(), data = R.mipmap.bg,contentScale = ContentScale.FillBounds)
                content()
            }
        })
    }


    inline fun <reified T: Activity> navActivity(){
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }
}
