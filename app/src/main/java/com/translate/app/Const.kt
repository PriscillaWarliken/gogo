package com.translate.app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.pm.PackageInfoCompat
import com.google.gson.JsonObject

object Const {

    interface AdConst{
        companion object{

            const val TYPE_NAV = "nav"
            const val TYPE_INT = "int"
            const val TYPE_OPEN = "start"

            const val AD_START = "start"
            const val AD_TEXT = "text"
            const val AD_OTHER = "other"
            const val AD_INITIAL = "initial"
            const val AD_INSERT = "insert"
            const val CLICK_TIME = "CLICK_TIME"
            const val CLICK_COUNT = "CLICK_COUNT"
            const val permissionNav = "canLoadNav"
        }
    }

    const val START_TIME = "START_TIME"
    const val TARGET_LANGUAGE = "TARGET_LANGUAGE"
    const val SOURCE_LANGUAGE = "SOURCE_LANGUAGE"
    const val RECENT_LANGUAGE = "RECENT_LANGUAGE"
    const val PRIVACY_AGREE = "PRIVACY_AGREE"
    const val USE_APP_TIME = "USE_APP_TIME"
    const val TRANSLATE_COUNT = "TRANSLATE_COUNT"
    const val START_EXPORT = "START_EXPORT"
    const val HOME_EXPORT = "HOME_EXPORT"

    val pName: String get() = App.context.packageName

    val releaseId: Int get() {
        return try {
            val info = App.context.packageManager.getPackageInfo(pName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageInfoCompat.getLongVersionCode(info).toInt()
            } else {
                info.versionCode
            }
        } catch (e: Exception) {
            0
        }
    }

    val versionName: String get() {
        return try {
            val info =
                App.context.packageManager.getPackageInfo(pName, 0)
            info.versionName
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    private val device_num: String @SuppressLint("HardwareIds")
    get() = Settings.Secure.getString(App.context.contentResolver, Settings.Secure.ANDROID_ID)

    private val sdk: Int get() = Build.VERSION.SDK_INT

    val softLang: String get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        App.context.resources.configuration.locales[0].language
    } else {
        App.context.resources.configuration.locale.language
    }

    val state_name: String get() {
        return try {
            (App.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).simCountryIso
        } catch (e: Exception) {
            ""
        }
    }

    private val current: Long get() = System.currentTimeMillis()

    private val plmn: String get() {
        return try {
            (App.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).let {
                it.networkOperator.ifEmpty { it.simOperator }
            }
        } catch (e: Exception) {
            ""
        }
    }

    val stateSim: String get() {
        return try {
            (App.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).simCountryIso
        } catch (e: Exception) {
            ""
        }
    }

    private val openTime: Long get() {
        return try {
            App.context.packageManager.getPackageInfo(pName, 0).firstInstallTime
        } catch (e: Exception) {
            0
        }
    }

    var baseParam: JsonObject = JsonObject().apply {
        addProperty("pName", pName)
        addProperty("releaseId", releaseId)
        addProperty("device_num", device_num)
        addProperty("advertNum", "")
        addProperty("sdk", sdk)
        addProperty("softLang", softLang)
        addProperty("state_name", state_name)
        addProperty("current", current)
        addProperty("plmn", plmn)
        addProperty("stateSim", stateSim)
        addProperty("openTime", openTime)
    }
}