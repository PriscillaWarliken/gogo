package com.translate.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.nguyenhoanglam.imagepicker.model.CustomColor
import com.nguyenhoanglam.imagepicker.model.CustomMessage
import com.nguyenhoanglam.imagepicker.model.GridCount
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.model.ImagePickerConfig
import com.nguyenhoanglam.imagepicker.model.IndicatorType
import com.nguyenhoanglam.imagepicker.model.RootDirectory
import com.nguyenhoanglam.imagepicker.ui.imagepicker.registerImagePicker
import com.translate.app.App
import com.translate.app.Const
import com.translate.app.R
import com.translate.app.ads.AdManager
import com.translate.app.ads.base.AdWrapper
import com.translate.app.ads.callback.IntAdCallback
import com.translate.app.ads.callback.NavAdCallback
import com.translate.app.ads.getMoringTime
import com.translate.app.repository.Repository
import com.translate.app.ui.languagePage.LanguageActivity
import com.translate.app.ui.ocrPage.CaptureActivity
import com.translate.app.ui.ocrPage.OCRActivity
import com.translate.app.ui.translatePage.TranslateActivity
import com.translate.app.ui.weight.CoilImage
import com.translate.app.ui.weight.NativeAdsView
import com.translate.app.ui.weight.click


class MainActivity : BaseActivity(),NavAdCallback,IntAdCallback {

    private val ABLUM_TAG = 0
    private val CAPTURE_TAG = 1
    private val TRANSLATE_TAG = 2
    private var clickTag = TRANSLATE_TAG
    private var images = ArrayList<Image>()
    private val launcher = registerImagePicker {
        if (it.isNullOrEmpty()) {
            return@registerImagePicker
        }
        images = it
        val image = images.first()

        val intent = Intent(this, OCRActivity::class.java)
        intent.putExtra("PATH", "${image.uri.path}")
        startActivity(intent)
    }
    private var showPopupWindowState by mutableStateOf(value = false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Repository.sharedPreferences.getLong(Const.USE_APP_TIME, 0L) < getMoringTime()) {
            //重置今天使用翻译次数次数
            Repository.sharedPreferences.edit {
                putInt(Const.TRANSLATE_COUNT,0)
            }
        }
        setContent {
            BackHandler(enabled = true) {}
            Box(modifier = Modifier
                .fillMaxSize()
                .click {
                    showPopupWindowState = false
                }){
                Column(modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 14.dp, top = 21.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Translate", fontSize = 34.sp)
                        CoilImage(modifier = Modifier
                            .size(50.dp)
                            .click {
                                if (showPopupWindowState) {
                                    showPopupWindowState = false
                                    return@click
                                }
                                showPopupWindowState = true
                            }, data = R.mipmap.settings)
                    }

                    Box(
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .click {
                                clickTag = TRANSLATE_TAG
                                spacerAdCount()
                            }
                    ){
                        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("anim/首页文本输入框.json"))
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            contentScale = ContentScale.None
                        )
                        Text(
                            text = "Enter the text to be\n translated...",
                            fontSize = 22.sp,
                            color = Color.White,
                            modifier = Modifier.padding(start = 20.dp, top = 20.dp)
                        )
                    }

                    adWrapper.value?.let {
                        NativeAdsView(
                            adWrapper = it, modifier = Modifier
                                .padding(top = 20.dp)
                                .padding(horizontal = 20.dp)
                        )
                    }
                }

                PopupWindws(
                    modifier = Modifier
                        .padding(top = 109.dp, end = 16.dp)
                        .shadow(10.dp, shape = RoundedCornerShape(16.dp))
                        .align(Alignment.TopEnd)
                        .size(200.dp, 120.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(start = 20.dp)
                )

                Row(modifier = Modifier
                    .padding(bottom = 57.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Box {
                        CoilImage(modifier = Modifier
                            .size(155.dp, 128.dp)
                            .click {
                                clickTag = ABLUM_TAG
                                spacerAdCount()
                            }, data = R.mipmap.home_album)
                        Text(text = "Album", fontSize = 18.sp,modifier = Modifier
                            .padding(bottom = 16.dp)
                            .align(Alignment.BottomCenter))
                    }
                    Box {
                        CoilImage(modifier = Modifier
                            .size(155.dp, 128.dp)
                            .click {
                                clickTag = CAPTURE_TAG
                                spacerAdCount()
                            }, data = R.mipmap.home_camera
                    )
                        Text(text = "Camera", fontSize = 18.sp,modifier = Modifier
                            .padding(bottom = 16.dp)
                            .align(Alignment.BottomCenter))
                    }
                }
            }

        }
    }

    private fun spacerAdCount() {
        if (Repository.sharedPreferences.getInt(Const.TRANSLATE_COUNT,0) == 1){
            showIntAd()
        }else{
            jumpNextActivity()
        }
        Repository.sharedPreferences.apply {
            var count = getInt(Const.TRANSLATE_COUNT, 0)
            edit {
                putInt(Const.TRANSLATE_COUNT,++count)
            }
        }
    }

    @Composable
    private fun PopupWindws(modifier:Modifier) {
        if (showPopupWindowState) {
            Column(
                modifier = modifier,
            ) {
                Row(modifier = Modifier.height(60.dp), verticalAlignment = Alignment.CenterVertically) {
                    CoilImage(modifier = Modifier
                        .padding(end = 7.dp)
                        .size(24.dp), data = R.mipmap.terms)
                    Text(text = "Terms of Service", fontSize = 16.sp)
                }
                Row(modifier = Modifier.height(60.dp), verticalAlignment = Alignment.CenterVertically) {
                    CoilImage(modifier = Modifier
                        .padding(end = 7.dp)
                        .size(24.dp), data = R.mipmap.privacy)
                    Text(text = "Privacy Policy", fontSize = 16.sp)
                }
            }
        }
    }

    private fun start() {
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

        launcher.launch(config,ImagePickerActivity::class.java)
    }

    override fun onStart() {
        super.onStart()
        if (App.isBackground.not()) {
            AdManager.setSmallCallBack(this, Const.AdConst.AD_TEXT)
            AdManager.getAdObjFromPool(Const.AdConst.AD_TEXT)
        }
    }

    override fun getNavAdFromPool(adWrapper: AdWrapper) {
        this.adWrapper.value=adWrapper
    }


    private fun showIntAd() {
        AdManager.setFullCallBack(this)
        AdManager.getAdObjFromPool(Const.AdConst.AD_INSERT)
    }

    override fun getIntAdFromPool(adWrapper: AdWrapper?) {
        adWrapper?.let {
            it.showAdInstance(this)
            return
        }
        jumpNextActivity()
    }

    override fun onCloseIntAd() {
        jumpNextActivity()
    }

    private fun jumpNextActivity() {
        when (clickTag) {
            TRANSLATE_TAG -> {
                navActivity<TranslateActivity>()
            }

            ABLUM_TAG -> {
                start()
            }

            CAPTURE_TAG -> {
                navActivity<CaptureActivity>()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        showPopupWindowState = false
    }
}

@Composable
fun TopBar() {
    val context = LocalContext.current as BaseActivity
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ){
        CoilImage(modifier = Modifier
            .padding(start = 15.dp)
            .align(Alignment.CenterStart)
            .size(24.dp)
            .click {
                context.finish()
            }, data = R.mipmap.universal_back)

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(243.dp)
                .height(44.dp)
                .background(color = Color.White, shape = RoundedCornerShape(76.dp))
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 20.dp)
                    .align(Alignment.CenterStart)
                    .click {
                        LanguageActivity.sourceSelectState = true
                        context.navActivity<LanguageActivity>()
                    }
            ) {
                Text(
                    text = Repository.sourceLanguage!!.languageEn, fontSize = 16.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1, modifier = Modifier.widthIn(max = 60.dp)
                )
                CoilImage(
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .size(11.dp, 6.dp),
                    data = R.mipmap.home_pulldown
                )
            }
            CoilImage(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(34.dp, 25.dp),
                data = R.mipmap.home_cut
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(end = 20.dp)
                    .align(Alignment.CenterEnd)
                    .click {
                        LanguageActivity.sourceSelectState = false
                        context.navActivity<LanguageActivity>()
                    }
            ) {
                Text(text = Repository.targetLanguage!!.languageEn, fontSize = 16.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,modifier = Modifier.widthIn(max = 60.dp))
                CoilImage(
                    modifier = Modifier
                        .padding(end = 2.dp)
                        .size(11.dp, 6.dp),
                    data = R.mipmap.home_pulldown
                )
            }
        }
    }
}
