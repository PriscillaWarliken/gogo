package com.translate.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.ads.nativead.NativeAd
import com.nguyenhoanglam.imagepicker.model.CustomColor
import com.nguyenhoanglam.imagepicker.model.CustomMessage
import com.nguyenhoanglam.imagepicker.model.GridCount
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.model.ImagePickerConfig
import com.nguyenhoanglam.imagepicker.model.IndicatorType
import com.nguyenhoanglam.imagepicker.model.RootDirectory
import com.nguyenhoanglam.imagepicker.ui.imagepicker.registerImagePicker
import com.translate.app.Const
import com.translate.app.R
import com.translate.app.ads.AdManager
import com.translate.app.ads.base.AdWrapper
import com.translate.app.ads.callback.IntAdCallback
import com.translate.app.repository.Repository
import com.translate.app.ui.languagePage.LanguageActivity
import com.translate.app.ui.ocrPage.OCRActivity
import com.translate.app.ui.weight.CoilImage

open class BaseActivity : ComponentActivity(), IntAdCallback {

    protected var adWrapper= mutableStateOf<NativeAd?>(null)
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

    override fun onStop() {
        super.onStop()
        adWrapper.value = null
    }

    inline fun <reified T: Activity> navActivity(){
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }

    private var images = ArrayList<Image>()
    private val launcher = registerImagePicker {
        canShowNav = true
        if (it.isNullOrEmpty()) {
            return@registerImagePicker
        }
        images = it
        val image = images.first()

        val intent = Intent(this, OCRActivity::class.java)
        intent.putExtra("PATH", "${image.uri.path}")
        startActivity(intent)
    }

    protected fun start(isFinish:Boolean = false) {
        canShowNav = false
        images.clear()
        val folderMode = false
        val multiSelectMode = false
        val cameraMode = false
        val showCamera = false
        val selectAllEnabled = false
        val unselectAllEnabled = false
        val showNumberIndicator = false
        val enableImageTransition = false

        val config = ImagePickerConfig(
            clazz = LanguageActivity::class.java,
            isfinish = isFinish,
            isCameraMode = cameraMode,
            isMultiSelectMode = multiSelectMode,
            isFolderMode = folderMode,
            isShowCamera = showCamera,
            isSelectAllEnabled = selectAllEnabled,
            isUnselectAllEnabled = unselectAllEnabled,
            isImageTransitionEnabled = enableImageTransition,
            selectedIndicatorType = if (showNumberIndicator) IndicatorType.NUMBER else IndicatorType.CHECK_MARK,
            limitSize = 100,
            rootDirectory = RootDirectory.DCIM,
            subDirectory = "Image Picker",
            folderGridCount = GridCount(2, 4),
            imageGridCount = GridCount(3, 5),
            selectedImages = images,
            customColor = CustomColor(
                background = "#000000",
                statusBar = "#000000",
                toolbar = "#212121",
                toolbarTitle = "#FFFFFF",
                toolbarIcon = "#FFFFFF",
                doneButtonTitle = "#FFFFFF",
                snackBarBackground = "#323232",
                snackBarMessage = "#FFFFFF",
                snackBarButtonTitle = "#4CAF50",
                loadingIndicator = "#757575",
                selectedImageIndicator = "#1976D2"
            ),
            customMessage = CustomMessage(
                reachLimitSize = "You can only select up to 10 images.",
                cameraError = "Unable to open camera.",
                noCamera = "Your device has no camera.",
                noImage = "No image found.",
                noPhotoAccessPermission = "Please allow permission to access photos and media.",
                noCameraPermission = "Please allow permission to access camera."
            ),
        )

        launcher.launch(config, ImagePickerActivity::class.java)
    }


    protected fun showInt() {
        if (Repository.sharedPreferences.getInt(Const.TRANSLATE_COUNT,0) == 1){
            showIntAd()
        }
        Repository.sharedPreferences.apply {
            var count = getInt(Const.TRANSLATE_COUNT, 0)
            edit {
                putInt(Const.TRANSLATE_COUNT,++count)
            }
        }
    }

    private fun showIntAd() {
        AdManager.setIntAdCallBack(this)
        AdManager.getAdObjFromPool(Const.AdConst.AD_INSERT)
    }

    override fun getIntAdFromPool(adWrapper: AdWrapper?) {
        adWrapper?.let {
            it.showAdInstance(this)
            return
        }
    }

    override fun onCloseIntAd() {}

    companion object{
        var canShowNav = true
    }
}
