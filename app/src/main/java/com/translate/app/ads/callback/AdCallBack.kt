package com.translate.app.ads.callback

interface AdCallBack {

    fun onLoadSuccess(adInstance: Any)
    fun onLoadFail(code: String, msg: String)
    fun onShow()
    fun onClose()
    fun onClick()
}