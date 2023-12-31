/*
 * Copyright (C) 2023 Image Picker
 * Author: Nguyen Hoang Lam <hoanglamvn90@gmail.com>
 */

package com.nguyenhoanglam.imagepicker.ui.imagepicker

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.nguyenhoanglam.imagepicker.helper.Constants
import com.nguyenhoanglam.imagepicker.helper.DeviceHelper
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.model.ImagePickerConfig
import com.nguyenhoanglam.imagepicker.ui.camera.CameraActivity

typealias ImagePickerCallback = (ArrayList<Image>) -> Unit

class ImagePickerLauncher(
    private val context: () -> Context,
    private val resultLauncher: ActivityResultLauncher<Intent>
) {
    fun launch(config: ImagePickerConfig = ImagePickerConfig(), clazz: Class<*>) {
        val intent = createImagePickerIntent(context(), config,clazz)
        resultLauncher.launch(intent)
    }
}

private fun createImagePickerIntent(context: Context, config: ImagePickerConfig, clazz: Class<*>): Intent {
    val intent: Intent = Intent(context, clazz)
    intent.putExtra(Constants.EXTRA_CONFIG, config)
    return intent
}

@Suppress("DEPRECATION")
fun getImages(data: Intent?): ArrayList<Image> {
    return if (data != null)
        if (DeviceHelper.isMinSdk33)
            data.getParcelableArrayListExtra(
                Constants.EXTRA_IMAGES,
                Image::class.java
            )!!
        else data.getParcelableArrayListExtra(Constants.EXTRA_IMAGES)!!
    else arrayListOf()
}

fun ComponentActivity.registerImagePicker(
    context: () -> Context = { this },
    callback: ImagePickerCallback
): ImagePickerLauncher {
    return ImagePickerLauncher(context, createLauncher(callback))
}

fun Fragment.registerImagePicker(
    context: () -> Context = { requireContext() },
    callback: ImagePickerCallback
): ImagePickerLauncher {
    return ImagePickerLauncher(context, createLauncher(callback))
}

private fun ComponentActivity.createLauncher(callback: ImagePickerCallback): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val images = getImages(it.data)
        callback(images)
    }
}

private fun Fragment.createLauncher(callback: ImagePickerCallback): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val images = getImages(it.data)
        callback(images)
    }
}


