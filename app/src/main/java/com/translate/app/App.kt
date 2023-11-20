package com.translate.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.view.WindowCompat
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.translate.app.repository.Repository
import com.translate.app.ui.StartActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class App : Application(),ActivityLifecycleCallbacks {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context:Context
        var isBackground = false
        var coldStart = true
        lateinit var firebaseAnalytics:FirebaseAnalytics
        val coroutineScope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        firebaseAnalytics = Firebase.analytics
        registerActivityLifecycleCallbacks(this)
        Repository.init()
        coroutineScope.launch {
            try {
                val gid = AdvertisingIdClient.getAdvertisingIdInfo(context).id.toString()
                Const.baseParam.addProperty("advertNum",gid)
            } catch (_: Exception) { }
            launch { Repository.useCacheConfig() }
            launch { Repository.parseLanguageJson() }
        }
        initAdjust()
    }

    private fun initAdjust() {
        val appToken = BuildConfig.adjust_token
        val environment = if (BuildConfig.DEBUG) AdjustConfig.ENVIRONMENT_SANDBOX else AdjustConfig.ENVIRONMENT_PRODUCTION
        val config = AdjustConfig(this, appToken, environment)
        Adjust.onCreate(config)
        if (Repository.sharedPreferences.getBoolean(Const.ADJUST_INSTALL, true)) {
            InstallReferrerClient.newBuilder(context).build().apply {
                startConnection(object : InstallReferrerStateListener {
                    override fun onInstallReferrerSetupFinished(responseCode: Int) {
                        when (responseCode) {
                            InstallReferrerClient.InstallReferrerResponse.OK -> {
                                try {
                                    val event = AdjustEvent(BuildConfig.adjust_code)
                                    val referrerUrl = this@apply.installReferrer.installReferrer
                                    event.addCallbackParameter(BuildConfig.adjust_referrerUrl, referrerUrl)
                                    Adjust.trackEvent(event)
                                    Repository.sharedPreferences.edit().putBoolean(Const.ADJUST_INSTALL, false).apply()
                                } catch (_: Exception) { }
                            }
                            InstallReferrerClient.InstallReferrerResponse.PERMISSION_ERROR,
                            InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR,
                            InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED,
                            InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE->{}
                        }
                        this@apply.endConnection()
                    }

                    override fun onInstallReferrerServiceDisconnected() {}
                })
            }
        }
    }


    private var activityCount = 0

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        if (activity is AdActivity) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            activity.window.statusBarColor = Color.TRANSPARENT
        }
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
