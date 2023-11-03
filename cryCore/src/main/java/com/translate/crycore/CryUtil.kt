package com.translate.crycore

import android.content.Context

object CryUtil {

    /**
     * A native method that is implemented by the 'crycore' native library,
     * which is packaged with this application.
     */
    external fun init(context: Context):String
    external fun decry(byteArray: ByteArray, cryMode:Int, codeMode:Int):ByteArray
    external fun cry(byteArray: ByteArray, cryMode:Int, codeMode:Int):ByteArray
    const val MODE_BASE = 0
    const val MODE_DES_EBC = 1

    init {
        System.loadLibrary("crycore")
    }
}