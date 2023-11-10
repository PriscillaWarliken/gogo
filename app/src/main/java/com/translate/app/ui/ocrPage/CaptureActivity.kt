package com.translate.app.ui.ocrPage

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.nativead.NativeAd
import com.nguyenhoanglam.imagepicker.helper.PermissionHelper
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
import com.translate.app.ads.callback.NavAdCallback
import com.translate.app.ui.BaseActivity
import com.translate.app.ui.ImagePickerActivity
import com.translate.app.ui.TopBar
import com.translate.app.ui.languagePage.LanguageActivity
import com.translate.app.ui.pointLog
import com.translate.app.ui.weight.NativeAdsView
import com.translate.app.ui.weight.PermissDialog
import com.translate.app.ui.weight.PreViewMainLayout
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class CaptureActivity : BaseActivity(), NavAdCallback {

    private var showPermissionDialog by mutableStateOf(value = false)
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestAllPermission()
        pointLog("Camera_And","拍照页曝光")
        setContent {
            val lensFacing = remember {
                mutableStateOf(value = CameraSelector.LENS_FACING_BACK)
            }
            Box(modifier = Modifier
                .statusBarsPadding()
                .fillMaxSize()){
                Column (modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally){
                    TopBar()

                    adWrapper.value?.let {
                        NativeAdsView(isBig = false, mAdInstance = it,modifier = Modifier
                            .padding(top = 20.dp)
                            .padding(horizontal = 20.dp))
                    }
                }
                Column(modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()) {
                    PreViewMainLayout(
                        lensFacing.value,
                        takePhotoClick = {
                            takePhoto(it)
                        },
                        switchFacing = {
                            lensFacing.value = if (lensFacing.value == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            }else{
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        openAlbum = {
                            start()
                        })
                }
            }

            if (showPermissionDialog) {
                PermissDialog{showPermissionDialog = false}
            }
        }
    }

    private fun requestAllPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val readPermission = Manifest.permission.CAMERA
            when (PermissionHelper.checkPermission(this, readPermission)) {
                PermissionHelper.STATUS.GRANTED -> {}

                PermissionHelper.STATUS.NOT_GRANTED -> PermissionHelper.requestAllPermissions(
                    this, arrayOf(readPermission), REQUEST_CODE_CAPTURE
                )

                PermissionHelper.STATUS.DENIED -> PermissionHelper.requestAllPermissions(
                    this, arrayOf(readPermission), REQUEST_CODE_CAPTURE
                )

                else->{
                    showPermissionDialog = true
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ){
        when (requestCode) {
            REQUEST_CODE_CAPTURE->{
                if (PermissionHelper.hasGranted(grantResults).not()) {
                    finish()
                }
            }
            else ->{
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }


    private val fileNameFormat = "yyyy-MM-dd-HH-mm-ss-SSS"

    private fun takePhoto(imageCapture: ImageCapture) {
        startTakePhoto(
            imageCapture = imageCapture,
            outputDirectory = getOutputDirectory(),
            executor = Executors.newSingleThreadExecutor(),
            onSuccess = {
                ResultActivity.fromCamera = true
                Log.d("TAG_HQL", "takePhoto PATH: ${it.path}")
                val intent = Intent(this@CaptureActivity, OCRActivity::class.java)
                intent.putExtra("PATH", "${it.path}")
                startActivity(intent)
                finish()
            },
            onError = {
                Log.d("TAG_HQL", "takePhoto: ${it.message}")
            }
        )
    }

    fun startTakePhoto(
        imageCapture: ImageCapture,
        outputDirectory: File,
        executor: Executor,
        onSuccess: (Uri) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {

        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(fileNameFormat, Locale.CHINA).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onSuccess(savedUri)
            }
        })
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }


    override fun onStart() {
        super.onStart()
        if (App.isBackground.not()) {
            AdManager.setNativeCallBack(this, Const.AdConst.AD_OTHER)
            AdManager.getAdObjFromPool(Const.AdConst.AD_OTHER)
        }
    }


    override fun getNavAdFromPool(adWrapper: AdWrapper) {
        this.adWrapper.value=adWrapper.getAdInstance() as NativeAd
    }

    companion object{
        const val REQUEST_CODE_CAPTURE = 1
    }
}