package com.translate.app.ui.translatePage

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.gson.JsonArray
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
import com.translate.app.ads.callback.SmallAdCallback
import com.translate.app.repository.Repository
import com.translate.app.ui.BaseActivity
import com.translate.app.ui.ImagePickerActivity
import com.translate.app.ui.TopBar
import com.translate.app.ui.languagePage.LanguageActivity
import com.translate.app.ui.languagePage.LanguageChangeListener
import com.translate.app.ui.ocrPage.CaptureActivity
import com.translate.app.ui.ocrPage.OCRActivity
import com.translate.app.ui.weight.CoilImage
import com.translate.app.ui.weight.NativeAdsView
import com.translate.app.ui.weight.click
import kotlinx.coroutines.launch


class TranslateResultActivity : BaseActivity(),LanguageChangeListener, SmallAdCallback {

    val viewModel by viewModels<TranslateViewModel>()
    private var showAnimState by mutableStateOf(value = false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageActivity.setLanguageChangeListener(this)
        TranslateViewModel.reusltLiveData.observe(this){
            showAnimState = false
        }
        setContent {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TopBar()

                adWrapper.value?.let {
                    NativeAdsView(
                        isBig = false, adWrapper = it, modifier = Modifier
                            .padding(top = 20.dp)
                            .padding(horizontal = 20.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(0.95f)
                        .background(color = Color(0xFF4974C9), shape = RoundedCornerShape(24.dp))
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = TranslateViewModel.srcText,
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 125.dp)
                    )

                    Spacer(
                        modifier = Modifier
                            .padding(vertical = 20.dp)
                            .fillMaxWidth(0.9f)
                            .height(1.dp)
                            .background(color = Color.White)
                    )

                    Text(
                        text = TranslateViewModel.dstText,
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 149.dp)
                    )

                    CoilImage(
                        modifier = Modifier
                            .padding(bottom = 16.dp, end = 16.dp)
                            .align(Alignment.End)
                            .size(38.dp)
                            .click {
                                copyText(TranslateViewModel.dstText)

                            }, data = R.mipmap.home_copy
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Box {
                        CoilImage(
                            modifier = Modifier
                                .size(155.dp, 128.dp)
                                .click {
                                    start()
                                }, data = R.mipmap.home_album
                        )
                        Text(
                            text = "Album", fontSize = 18.sp, modifier = Modifier
                                .padding(bottom = 16.dp)
                                .align(Alignment.BottomCenter)
                        )
                    }
                    Box {
                        CoilImage(
                            modifier = Modifier
                                .size(155.dp, 128.dp)
                                .click {
                                    navActivity<CaptureActivity>()
                                }, data = R.mipmap.home_camera
                        )
                        Text(
                            text = "Camera",
                            fontSize = 18.sp,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .align(Alignment.BottomCenter)
                        )
                    }
                }
            }

            if (showAnimState) {
                BackHandler (enabled = true){}
                Box(modifier = Modifier
                    .click { }
                    .fillMaxSize()
                    .background(color = Color(0x4D000000))){
                    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("anim/文本翻译中.json"))
                    Column(modifier = Modifier
                        .align(Alignment.Center)) {
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            contentScale = ContentScale.None
                        )
                        Text(text = "Translating...",color = Color.White)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (App.isBackground.not()) {
            AdManager.setSmallCallBack(this, Const.AdConst.AD_TEXT)
            AdManager.getAdObjFromPool(Const.AdConst.AD_TEXT)
        }
    }

    var adWrapper= mutableStateOf<AdWrapper?>(null)
    override fun getSmallFromPool(adWrapper: AdWrapper) {
        this.adWrapper.value=adWrapper
    }

    /**
     * 将文本复制到剪贴板
     *
     * @param text 要复制的文本
     */
    fun copyText(text: String?) {
        val clipboardManager = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this@TranslateResultActivity, "Copied !", Toast.LENGTH_SHORT).show()
    }

    override fun changeLanguage(sourceLanguage: String, targetLanguage: String) {
        showAnimState = true
        val arr = JsonArray().apply {
            add(TranslateViewModel.srcText)
        }
        lifecycleScope.launch {
            viewModel.execTranslateApi(
                arr,
                Repository.sourceLanguage!!.language,
                Repository.targetLanguage!!.language
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        LanguageActivity.setLanguageChangeListener(null)
    }


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

    private fun start() {
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

        launcher.launch(config, ImagePickerActivity::class.java)
    }

}