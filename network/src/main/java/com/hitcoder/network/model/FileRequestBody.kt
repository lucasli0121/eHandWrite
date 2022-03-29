package com.hitcoder.network.model

import android.os.Build
import android.util.Log
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.internal.closeQuietly
import okio.BufferedSink
import okio.Source
import okio.source
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.URLConnection
import java.net.URLEncoder

/**
 * 自定义[RequestBody],适配[Build.VERSION_CODES.Q],支持传入[File]与[InputStream]
 *
 * 当同时传入[File]与[InputStream]时，优先使用[InputStream]
 */
class FileRequestBody(private val fileInput: FileInfo) : RequestBody() {

    init {
        fileInput.check()
    }

    override fun contentType(): MediaType? {
        return guessMimeType(fileInput.fileName).toMediaTypeOrNull()
    }

    override fun contentLength(): Long {
        return fileInput.file?.length() ?: fileInput.fileStream?.available()?.toLong() ?: -1L
    }

    override fun writeTo(sink: BufferedSink) {
        var source: Source? = null
        try {
            val stream =
                fileInput.fileStream ?: FileInputStream(fileInput.file!!)

            source = stream.source()
            sink.writeAll(source)
        } finally {
            source?.closeQuietly()
        }
    }

    private fun guessMimeType(path: String): String {
        val fileNameMap = URLConnection.getFileNameMap()
        var contentTypeFor = try {
            fileNameMap.getContentTypeFor(URLEncoder.encode(path, "UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            Log.e("PrintStackTrace", "PostFormRequest guessMimeType Exception ${e.message}")
            null
        }
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream"
        }
        return contentTypeFor
    }

}