package com.translate.app.repository.bean

data class ResultBean(
    val `data`: List<Data>,
    val returnStatus: Int
)

data class Data(
    val dst: String,
    val src: String
)