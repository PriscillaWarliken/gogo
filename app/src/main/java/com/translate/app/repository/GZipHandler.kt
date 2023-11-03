package com.translate.app.repository

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object GZipHandler {
    fun zipData(data: ByteArray): ByteArray =
        ByteArrayOutputStream().use { out ->
            GZIPOutputStream(out).also { it.write(data) }.close()
            out.toByteArray()
        }

    fun unZipData(data: ByteArray): ByteArray =
        ByteArrayOutputStream().use { out ->
            GZIPInputStream(data.inputStream()).use { gzip ->
                gzip.copyTo(out)
            }
            out.toByteArray()
        }
}