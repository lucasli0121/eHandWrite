package com.hitcoder.network.model

import okhttp3.MultipartBody
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

/**
 *文件信息封装类
 * @property key 生成[MultipartBody.Part]的Key值
 * @property fileName 生成[MultipartBody.Part]的文件名称
 * @property file 要上传的文件
 * @property fileStream 要上传的文件流
 * @see [file]与[fileStream]至少需要初始化一个，同时存在优先使用[fileStream]
 */
@Suppress("unused")
class FileInfo {

    var key: String = ""
    var fileName: String = ""
    var file: File? = null
    var fileStream: InputStream? = null

    constructor(key: String, fileName: String, file: File?) {
        this.key = key
        this.fileName = fileName
        this.file = file
    }

    constructor(key: String, fileName: String, fileStream: InputStream?) {
        this.key = key
        this.fileName = fileName
        this.fileStream = fileStream
    }

    fun check() {
        if (file == null && fileStream == null) {
            throw FileNotFoundException("参数 'file' 或 'fileStream' 至少设置一个，如果都设置，以 'fileStream' 为准")
        }
        if (fileStream == null && file != null && file?.exists() == false) {
            throw FileNotFoundException("文件不存在:$file")
        }
    }

}
