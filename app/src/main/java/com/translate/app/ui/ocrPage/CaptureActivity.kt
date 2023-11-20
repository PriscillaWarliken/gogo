package com.translate.app.ui.ocrPage

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.core.content.edit
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
import com.translate.app.repository.Repository
import com.translate.app.ui.BaseActivity
import com.translate.app.ui.ImagePickerActivity
import com.translate.app.ui.TopBar
import com.translate.app.ui.languagePage.LanguageActivity
import com.translate.app.ui.pointLog
import com.translate.app.ui.weight.NativeAdsView
import com.translate.app.ui.weight.PermissDialog
import com.translate.app.ui.weight.PreViewMainLayout
import com.translate.app.ui.weight.SmallNavView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class CaptureActivity : BaseActivity(), NavAdCallback {

    private var showPermissionDialog by mutableStateOf(value = false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageActivity.setLanguageChangeListener(null)
        pointLog("Camera_And","拍照页曝光")
        setContent {
            val lensFacing = remember {
                mutableStateOf(value = CameraSelector.LENS_FACING_BACK)
            }
            BackHandler(enabled = true) {
                showIntAd()
            }
            Box(modifier = Modifier
                .statusBarsPadding()
                .fillMaxSize()){
                Column (modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally){
                    TopBar(finishBlock = {
                        showIntAd()
                        finish()}
                    )

                    if (adWrapper.value == null) {
                        SmallNavView(modifier = Modifier
                            .padding(top = 20.dp)
                            .padding(horizontal = 20.dp))
                    }else{
                        NativeAdsView(isBig = false, mAdInstance = adWrapper.value!!,modifier = Modifier
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
                            start(true)
                            showInt()
                        })
                }
            }

            if (showPermissionDialog) {
                PermissDialog{showPermissionDialog = false}
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requestAllPermission()
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
        if (App.isBackground.not() && canShowNav) {
            AdManager.setNativeCallBack(this, Const.AdConst.AD_OTHER)
            AdManager.getAdObjFromPool(Const.AdConst.AD_OTHER)
        }
    }


    override fun getNavAdFromPool(adWrapper: AdWrapper) {
        this.adWrapper.value=adWrapper.getAdInstance() as NativeAd
    }


    private fun showIntAd() {
        if (Repository.extraAd_button.not()) {
            return
        }
        AdManager.setIntAdCallBack(this)
        AdManager.getAdObjFromPool(Const.AdConst.AD_INSERT)
    }

    companion object{
        const val REQUEST_CODE_CAPTURE = 1
    }
}