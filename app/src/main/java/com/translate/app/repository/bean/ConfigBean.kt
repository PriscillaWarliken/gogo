package com.translate.app.repository.bean

import com.google.gson.annotations.SerializedName

data class ConfigBean(
    val resp: Resp,
    val returnMessage: String,
    val status: Int
)
data class Resp(
    val adArrays: List<AdArray>,
    val bigBig:Int,
    val noNO:Int,
    val pullMin:Int,
    val extraAd_button:Boolean,
    val firstAdOpen_button:Boolean,
    val guide_button:Boolean
)

data class AdArray(
    val adOpen: Boolean,
    val adSource: List<InnerAd>,
    val advPlace: String
)

data class InnerAd(
    @SerializedName("ad_place")
    val ad_code: String,
    @SerializedName("cate_adv")
    val advFormat: String,
    @SerializedName("adv_weight")
    val adv_scale: Int,
    @SerializedName("closeSize")
    val closeSize: Int
)