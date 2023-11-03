package com.translate.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Rational
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraXConfig
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.translate.app.repository.Repository
import com.translate.app.ui.StartActivity
import com.translate.crycore.CryUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class App : Application(),ActivityLifecycleCallbacks {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context:Context
        var isBackground = false
        val coroutineScope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        registerActivityLifecycleCallbacks(this)
        Repository.init()
        coroutineScope.launch {
            launch { Repository.useCacheConfig() }
            launch { Repository.parseLanguageJson() }
        }
    }

    private var activityCount = 0


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        activityCount++
    }

    override fun onActivityResumed(activity: Activity) {
        if (isBackground) {
            isBackground = false
            if (activity is StartActivity) {
                return
            }
            activity.startActivity(Intent(activity,StartActivity::class.java))
        }
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        activityCount--
        if (activityCount <= 0) {
            isBackground = true
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

}
