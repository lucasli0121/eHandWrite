@file:Suppress("MemberVisibilityCanBePrivate")

package com.hitcoder.network.utils

import com.hitcoder.network.model.FileInfo
import com.hitcoder.network.model.FileRequestBody
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * 自定义[RequestBody]构建器，支持[FormBody]与[MultipartBody]的构建
 *
 * 使用方式：
 *
 * [FormBody]构建 ： 调用[addParam]或[addParams]传入参数，调用[buildFormBody]生成[FormBody]
 *
 * [MultipartBody]构建 ： [addParam]、[addParams]、[addFile]、[addFiles]传入参数，调用[buildMultipartBody]生成[MultipartBody]
 */
class RequestBodyBuilder {

    private val params = HashMap<String, String>()
    private val files = ArrayList<FileInfo>()

    /**
     * 添加文本类型参数
     * @param key 参数键
     * @param value 参数值
     */
    fun addParam(key: String, value: String): RequestBodyBuilder {
        params[key] = value
        return this
    }

    /**
     * 添加多个文本类型参数
     * @param paramMap 参数Map
     */
    fun addParams(paramMap: Map<String, String>): RequestBodyBuilder {
        paramMap.forEach {
            params[it.key] = it.value
        }
        return this
    }

    /**
     * 添加文件类型参数
     * @param fileInfo 文件信息
     */
    fun addFile(fileInfo: FileInfo): RequestBodyBuilder {
        files.add(fileInfo)
        return this
    }

    /**
     * 添加多个文件类型参数
     * @param fileInfo 文件信息
     */
    fun addFiles(fileInfo: List<FileInfo>): RequestBodyBuilder {
        files.addAll(fileInfo)
        return this
    }

    /**
     * 构建[FormBody]
     */
    fun buildFormBody(): FormBody {
        val builder = FormBody.Builder()
        params.forEach {
            builder.add(it.key, it.value)
        }
        return builder.build()
    }

    /**
     * 构建[MultipartBody]
     */
    fun buildMultipartBody(): MultipartBody {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        //构建文本参数
        params.forEach {
            builder.addPart(
                Headers.headersOf("Content-Disposition", "form-data; name=\"${it.key}\""),
                it.value.toRequestBody(null)
            )
        }
        //构建文件参数
        files.forEach {
            builder.addFormDataPart(
                it.key, it.getFileName(),
                FileRequestBody(it)
            )
        }

        return builder.build()
    }
}