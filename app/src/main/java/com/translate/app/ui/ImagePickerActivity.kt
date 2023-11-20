package com.translate.app.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nguyenhoanglam.imagepicker.R
import com.nguyenhoanglam.imagepicker.databinding.ImagepickerActivityImagepickerBinding
import com.nguyenhoanglam.imagepicker.helper.Constants
import com.nguyenhoanglam.imagepicker.helper.DeviceHelper
import com.nguyenhoanglam.imagepicker.helper.GlideHelper
import com.nguyenhoanglam.imagepicker.helper.ImageHelper
import com.nguyenhoanglam.imagepicker.helper.PermissionHelper
import com.nguyenhoanglam.imagepicker.helper.ToastHelper
import com.nguyenhoanglam.imagepicker.listener.OnFolderClickListener
import com.nguyenhoanglam.imagepicker.listener.OnImageSelectListener
import com.nguyenhoanglam.imagepicker.model.Folder
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.model.ImagePickerConfig
import com.nguyenhoanglam.imagepicker.ui.camera.CameraModule
import com.nguyenhoanglam.imagepicker.ui.camera.OnImageReadyListener
import com.nguyenhoanglam.imagepicker.ui.imagepicker.FolderFragment
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImageFragment
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePickerViewModel
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePickerViewModelFactory
import com.translate.app.App
import com.translate.app.Const
import com.translate.app.ads.AdManager
import com.translate.app.ads.base.AdWrapper
import com.translate.app.ads.callback.NavAdCallback
import com.translate.app.repository.Repository
import com.translate.app.repository.bean.LanguageBeanItem
import com.translate.app.ui.languagePage.LanguageActivity
import com.translate.app.ui.ocrPage.ResultActivity

class ImagePickerActivity : AppCompatActivity(), OnFolderClickListener, OnImageSelectListener,
    NavAdCallback {

    companion object{
        var sourceLanguage = ""
        var targetLanguage = ""

        fun setSourceLanguageMethod(string: String) {
            sourceLanguage = string
        }

        fun setTargetLanguageMethod(string: String) {
            targetLanguage = string
        }
    }

    private lateinit var binding: ImagepickerActivityImagepickerBinding
    private lateinit var config: ImagePickerConfig

    private lateinit var viewModel: ImagePickerViewModel
    private val cameraModule = CameraModule()

    private val backClickListener = View.OnClickListener { handleBackPress() }
    private val cameraClickListener = View.OnClickListener { captureImageWithPermission() }
    private val selectAllClickListener = View.OnClickListener { handleSelectAllImages() }
    private val unselectAllClickListener = View.OnClickListener { handleUnselectAllImages() }
    private val doneClickListener = View.OnClickListener { onDone() }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                cameraModule.saveImage(this@ImagePickerActivity,
                    config,
                    object : OnImageReadyListener {
                        override fun onImageReady(images: ArrayList<Image>) {
                            fetchDataWithPermission()
                        }

                        override fun onImageNotReady() {
                            fetchDataWithPermission()
                        }
                    })
            }
        }

    fun setStatusBarTextColor(window: Window, light: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var systemUiVisibility = window.decorView.systemUiVisibility
            systemUiVisibility = if (light) { //白色文字
                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else { //黑色文字
                systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            window.decorView.systemUiVisibility = systemUiVisibility
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent == null) {
            finish()
            return
        }
        pointLog("Album_And","相册页曝光")
        LanguageActivity.setLanguageChangeListener(null)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        var systemUiVisibility = window.decorView.systemUiVisibility
        systemUiVisibility =
            systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.systemUiVisibility = systemUiVisibility
        window.statusBarColor = Color.TRANSPARENT

        //设置状态栏文字颜色
        setStatusBarTextColor(window, true)


        config = if (DeviceHelper.isMinSdk33) intent.getParcelableExtra(
            Constants.EXTRA_CONFIG, ImagePickerConfig::class.java
        )!! else intent.getParcelableExtra(Constants.EXTRA_CONFIG)!!
        config.initDefaultValues(this@ImagePickerActivity)

        // Setup status bar theme

        GlideHelper.setConfig(
            config.isImageTransitionEnabled,
            config.customDrawable!!.loadingImagePlaceholder,
            config.customDrawable!!.errorImagePlaceholder
        )

        binding = ImagepickerActivityImagepickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this, ImagePickerViewModelFactory(this.application)
        )[ImagePickerViewModel::class.java]
        viewModel.apply {
            setConfig(config)
            result.observe(this@ImagePickerActivity) {
                val fragment = getCurrentFragment()
                if (fragment != null && fragment is ImageFragment) {
                    configSelectAllButtons(
                        fragment.getBucketId(), it.images, viewModel.selectedImages.value
                    )
                }
            }
            selectedImages.observe(this@ImagePickerActivity) {
                val fragment = getCurrentFragment()
                if (fragment != null && fragment is ImageFragment) {
                    configSelectAllButtons(
                        fragment.getBucketId(), viewModel.result.value?.images, it
                    )
                }
            }
        }

        setupViews()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        fetchDataWithPermission()
        binding.sourceLanguage.text = sourceLanguage
        binding.targetLanguage.text = targetLanguage
    }

    private fun setupViews() {
        binding.backIv.setOnClickListener {
            if (config.isfinish) {
                finish()
            }else{
                startActivity(Intent(this,MainActivity::class.java))
            }
            BaseActivity.canShowNav = true
        }

        binding.sourceLanguage.setOnClickListener {
            LanguageActivity.sourceSelectState = true
            startActivity(Intent(this,config.clazz))
        }
        binding.targetLanguage.setOnClickListener {
            LanguageActivity.sourceSelectState = false
            startActivity(Intent(this,config.clazz))
        }
        binding.iv2.setOnClickListener {
            var temp: LanguageBeanItem?
            if (Repository.sourceLanguage != null && Repository.targetLanguage != null) {
                temp = Repository.sourceLanguage
                Repository.sourceLanguage = Repository.targetLanguage
                Repository.targetLanguage = temp
                sourceLanguage = Repository.sourceLanguage!!.languageEn
                targetLanguage = Repository.targetLanguage!!.languageEn
                binding.sourceLanguage.text = sourceLanguage
                binding.targetLanguage.text = targetLanguage
            }
        }

//        binding.snackbar.config(config)

        val initialFragment =
            if (config.isFolderMode) FolderFragment.newInstance(config.folderGridCount)
            else ImageFragment.newInstance(config.imageGridCount)

        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, initialFragment)
            .commit()
    }

    private fun getCurrentFragment(): Fragment? =
        supportFragmentManager.findFragmentById(R.id.fragmentContainer)


    private fun isFolderFragment(): Boolean {
        val fragment = getCurrentFragment()
        return fragment != null && fragment is FolderFragment
    }

    private fun handleBackPress() {
        if (config.isfinish) {
            finish()
        }else{
            startActivity(Intent(this,MainActivity::class.java))
        }
        BaseActivity.canShowNav = true
    }

    private fun fetchDataWithPermission() {
        val readPermission =
            if (DeviceHelper.isMinSdk33) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE

        when (PermissionHelper.checkPermission(this@ImagePickerActivity, readPermission)) {
            PermissionHelper.STATUS.GRANTED -> fetchData()

            PermissionHelper.STATUS.NOT_GRANTED -> PermissionHelper.requestAllPermissions(
                this@ImagePickerActivity, arrayOf(readPermission), Constants.RC_READ_PERMISSION
            )

            PermissionHelper.STATUS.DENIED -> PermissionHelper.requestAllPermissions(
                this@ImagePickerActivity, arrayOf(readPermission), Constants.RC_READ_PERMISSION
            )

            else->{}
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.RC_READ_PERMISSION -> {
                if (PermissionHelper.hasGranted(grantResults)) {
                    fetchData()
                } else {
                    finish()
                }
            }

            Constants.RC_WRITE_PERMISSION, Constants.RC_CAMERA_PERMISSION -> {
                if (PermissionHelper.hasGranted(grantResults)) {
                    captureImage()
                }
            }

            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun fetchData() = viewModel.fetchImages()

    private fun onDone() {
        val selectedImages = viewModel.selectedImages.value
        finishPickImages(selectedImages ?: arrayListOf())
    }

    private fun handleSelectAllImages() {
        val fragment = getCurrentFragment()
        if (fragment != null && fragment is ImageFragment) {
            fragment.selectAllImages()
        }
    }

    private fun handleUnselectAllImages() {
        val fragment = getCurrentFragment()
        if (fragment != null && fragment is ImageFragment) {
            fragment.unselectAllImages()
        }
    }

    private fun configSelectAllButtons(
        bucketId: Long?, images: ArrayList<Image>?, selectedImages: ArrayList<Image>?
    ) {
        if (!config.isMultiSelectMode) return

        val state = ImageHelper.getBucketSelectionState(
            images, selectedImages, bucketId
        )
    }

    private fun captureImageWithPermission() {
        val isCameraPermissionDeclared = PermissionHelper.isPermissionDeclared(
            this@ImagePickerActivity, Manifest.permission.CAMERA
        )

        if (DeviceHelper.isMinSdk29) {
            if (isCameraPermissionDeclared) {
                val cameraPermission = Manifest.permission.CAMERA
                when (PermissionHelper.checkPermission(
                    this@ImagePickerActivity, cameraPermission
                )) {
                    PermissionHelper.STATUS.GRANTED -> captureImage()

                    PermissionHelper.STATUS.NOT_GRANTED -> PermissionHelper.requestAllPermissions(
                        this@ImagePickerActivity,
                        arrayOf(cameraPermission),
                        Constants.RC_CAMERA_PERMISSION
                    )

                    PermissionHelper.STATUS.DENIED -> PermissionHelper.requestAllPermissions(
                        this@ImagePickerActivity,
                        arrayOf(cameraPermission),
                        Constants.RC_CAMERA_PERMISSION
                    )

                    else -> {}
                }
            } else {
                captureImage()
            }
        } else {
            if (isCameraPermissionDeclared) {
                val statuses = PermissionHelper.checkPermissions(
                    this@ImagePickerActivity,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                )

                if (statuses[0] == PermissionHelper.STATUS.GRANTED && statuses[1] == PermissionHelper.STATUS.GRANTED) {
                    captureImage()
                } else if (statuses[0] == PermissionHelper.STATUS.DISABLED && statuses[1] == PermissionHelper.STATUS.DISABLED) {

                } else if (statuses[0] == PermissionHelper.STATUS.DISABLED) {

                } else if (statuses[1] == PermissionHelper.STATUS.DISABLED) {

                } else {
                    val requestPermissions = ArrayList<String>()
                    for ((index, value) in statuses.withIndex()) {
                        if (value == PermissionHelper.STATUS.NOT_GRANTED || value == PermissionHelper.STATUS.DENIED) {
                            requestPermissions.add(if (index == 0) Manifest.permission.CAMERA else Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    }

                    PermissionHelper.requestAllPermissions(
                        this@ImagePickerActivity,
                        requestPermissions.toTypedArray(),
                        Constants.RC_CAMERA_PERMISSION
                    )
                }
            } else {
                val writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                when (PermissionHelper.checkPermission(
                    this,
                    writePermission,
                )) {
                    PermissionHelper.STATUS.GRANTED -> captureImage()

                    PermissionHelper.STATUS.NOT_GRANTED -> PermissionHelper.requestAllPermissions(
                        this@ImagePickerActivity,
                        arrayOf(writePermission),
                        Constants.RC_WRITE_PERMISSION
                    )

                    PermissionHelper.STATUS.DENIED -> PermissionHelper.requestAllPermissions(
                        this@ImagePickerActivity,
                        arrayOf(writePermission),
                        Constants.RC_WRITE_PERMISSION
                    )

                    else -> {}
                }
            }
        }
    }


    private fun captureImage() {
        if (!DeviceHelper.checkCameraAvailability(this)) {
            return
        }

        val intent = cameraModule.getCameraIntent(this@ImagePickerActivity, config)
        if (intent == null) {
            ToastHelper.show(this, getString(R.string.imagepicker_error_camera))
            return
        }
        resultLauncher.launch(intent)
    }

    private fun finishPickImages(images: ArrayList<Image>) {
        ResultActivity.fromCamera = false
        val data = Intent()
        data.putParcelableArrayListExtra(Constants.EXTRA_IMAGES, images)
        setResult(Activity.RESULT_OK, data)
        finish()
    }


    override fun onFolderClick(folder: Folder) {
        supportFragmentManager.beginTransaction().add(
            R.id.fragmentContainer,
            ImageFragment.newInstance(folder.bucketId, config.imageGridCount)
        ).addToBackStack(null).commit()

        configSelectAllButtons(
            folder.bucketId, viewModel.result.value?.images, viewModel.selectedImages.value
        )
    }

    override fun onSelectedImagesChanged(selectedImages: ArrayList<Image>) {
        viewModel.selectedImages.value = selectedImages
    }

    override fun onSingleModeImageSelected(image: Image) {
        finishPickImages(ImageHelper.singleListFromImage(image))
    }

    override fun onStart() {
        super.onStart()
        if (App.isBackground.not()) {
            AdManager.setNativeCallBack(this, Const.AdConst.AD_OTHER)
            AdManager.getAdObjFromPool(Const.AdConst.AD_OTHER)
        }
    }

    override fun getNavAdFromPool(adWrapper: AdWrapper) {
        binding.adLayout.removeAllViews()
        adWrapper.showAdInstance(this,binding.adLayout,false)
    }
}