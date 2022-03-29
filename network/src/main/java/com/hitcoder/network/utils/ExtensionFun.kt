@file:Suppress("DEPRECATION", "unused", "KDocUnresolvedReference")

package com.hitcoder.network.utils

import android.net.ParseException
import android.text.TextUtils
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.hitcoder.network.exceptions.ServerException
import com.hitcoder.network.model.FileInfo
import com.hitcoder.network.model.NetworkError
import com.hitcoder.network.model.ResponseData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.toHexString
import org.apache.http.conn.ConnectTimeoutException
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.net.UnknownHostException
import java.security.MessageDigest
import javax.net.ssl.SSLHandshakeException

const val UNAUTHORIZED = 401
const val FORBIDDEN = 403
const val NOT_FOUND = 404
const val REQUEST_TIMEOUT = 408
const val INTERNAL_SERVER_ERROR = 500
const val BAD_GATEWAY = 502
const val SERVICE_UNAVAILABLE = 503
const val GATEWAY_TIMEOUT = 504

internal const val NETWORK_ENVIRONMENT_PREF_KEY = "network_environment_type"
internal const val ERROR_MSG_CONNECT_ERROR = "网络连接失败"
internal const val ERROR_MSG_NET_ERROR = "网络异常"
internal const val ERROR_MSG_PARSE_ERROR = "解析错误"
internal const val ERROR_MSG_CREATE_FILE_FAIL = "文件创建失败，请重试"
internal const val ERROR_MSG_DOWNLOAD_FAIL = "文件下载失败"
internal const val ERROR_MSG_DOWNLOAD_CANCEL = "取消下载文件"
internal const val ERROR_MSG_TIMEOUT = "连接超时"
internal const val ERROR_MSG_FILE_READ_ERROR = "文件读取失败"
internal const val ERROR_MSG_CERTIFICATE_FAILED = "证书验证失败"
internal const val ERROR_MSG_UNKNOW = "未知错误"
internal const val ERROR_MSG_CANCEL = "取消网络访问"


/**
 * 给[Call]增加的扩展函数，同步执行网络接口，对各种异常进行封装
 */
fun <T> Call<T>.executeEx(handler: ((T?) -> Unit)? = null): ResponseData<T> {
    try {
        val execute = execute()
        if (!execute.isSuccessful) {
            return ResponseData(NetworkError.HTTP_ERROR.code, ERROR_MSG_NET_ERROR, null)
        }
        handler?.invoke(execute.body())
        return ResponseData(NetworkError.SUCCESS.code, "成功", execute.body())
    } catch (e: Throwable) {
        //进行异常捕获封装
        return when (e) {
            is HttpException -> {
                when (e.code()) {
                    UNAUTHORIZED,
                    FORBIDDEN,
                    NOT_FOUND,
                    REQUEST_TIMEOUT,
                    GATEWAY_TIMEOUT,
                    INTERNAL_SERVER_ERROR,
                    BAD_GATEWAY,
                    SERVICE_UNAVAILABLE ->
                        ResponseData(NetworkError.HTTP_ERROR.code, ERROR_MSG_NET_ERROR, null)
                    else -> ResponseData(NetworkError.HTTP_ERROR.code, ERROR_MSG_NET_ERROR, null)
                }
            }

            is JsonParseException, is JSONException, is ParseException ->
                ResponseData(NetworkError.PARSE_ERROR.code, ERROR_MSG_PARSE_ERROR, null)

            is ConnectException, is UnknownHostException ->
                ResponseData(NetworkError.NETWORD_ERROR.code, ERROR_MSG_CONNECT_ERROR, null)

            is SSLHandshakeException ->
                ResponseData(NetworkError.SSL_ERROR.code, ERROR_MSG_CERTIFICATE_FAILED, null)

            is ConnectTimeoutException, is SocketTimeoutException ->
                ResponseData(NetworkError.TIMEOUT_ERROR.code, ERROR_MSG_TIMEOUT, null)

            is IOException -> {
                if (TextUtils.equals("Canceled", e.message)) {
                    ResponseData(NetworkError.CANCEL.code, ERROR_MSG_CANCEL, null)
                } else {
                    ResponseData(NetworkError.FILE_ERROR.code, ERROR_MSG_FILE_READ_ERROR, null)
                }
            }

            //用户自己抛出的异常
            is ServerException -> ResponseData(e.code, e.message, null)

            else -> ResponseData(NetworkError.UNKNOWN.code, ERROR_MSG_UNKNOW, null)
        }
    }
}

/**
 * 给[Call]增加的扩展函数，异步执行网络接口，对各种异常进行封装
 */
fun <T> Call<T>.enqueueEx(
    callback: (code: Int, message: String, data: T?) -> Unit,
    handler: ((T?) -> Unit)? = null
): Call<T> {

    /**
     * 内部函数，异常处理
     */
    fun exceptionHandler(t: Throwable) {
        when (t) {
            is HttpException -> {
                when (t.code()) {
                    UNAUTHORIZED,
                    FORBIDDEN,
                    NOT_FOUND,
                    REQUEST_TIMEOUT,
                    GATEWAY_TIMEOUT,
                    INTERNAL_SERVER_ERROR,
                    BAD_GATEWAY,
                    SERVICE_UNAVAILABLE ->
                        callback(NetworkError.HTTP_ERROR.code, ERROR_MSG_NET_ERROR, null)
                    else -> callback(
                        NetworkError.HTTP_ERROR.code,
                        ERROR_MSG_NET_ERROR,
                        null
                    )
                }
            }

            is JsonParseException, is JSONException, is ParseException ->
                callback(NetworkError.PARSE_ERROR.code, ERROR_MSG_PARSE_ERROR, null)

            is ConnectException, is UnknownHostException ->
                callback(NetworkError.NETWORD_ERROR.code, ERROR_MSG_CONNECT_ERROR, null)

            is SSLHandshakeException ->
                callback(NetworkError.SSL_ERROR.code, ERROR_MSG_CERTIFICATE_FAILED, null)

            is ConnectTimeoutException, is SocketTimeoutException ->
                callback(NetworkError.TIMEOUT_ERROR.code, ERROR_MSG_TIMEOUT, null)

            is IOException -> {
                if (TextUtils.equals("Canceled", t.message)) {
                    callback(NetworkError.CANCEL.code, ERROR_MSG_CANCEL, null)
                } else {
                    callback(NetworkError.FILE_ERROR.code, ERROR_MSG_FILE_READ_ERROR, null)
                }
            }

            //用户自己抛出的异常
            is ServerException -> callback(t.code, t.message, null)

            else -> callback(NetworkError.UNKNOWN.code, ERROR_MSG_UNKNOW, null)
        }
    }

    enqueue(object : Callback<T> {
        override fun onFailure(call: Call<T>, t: Throwable) {
            exceptionHandler(t)
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            val body = response.body()
            if (!response.isSuccessful) {
                callback(NetworkError.HTTP_ERROR.code, ERROR_MSG_NET_ERROR, null)
                return
            }
            try {
                handler?.invoke(body)
            } catch (e: Throwable) {
                exceptionHandler(e)
                return
            }
            callback(NetworkError.SUCCESS.code, "成功", body)
        }
    })

    return this
}

/**
 * 获取 '[URLEncoder]'加密后的 '[fileName]'
 */
internal fun FileInfo.getFileName(): String = try {
    URLEncoder.encode(fileName, "UTF-8")
} catch (e: UnsupportedEncodingException) {
    fileName
}

/**
 * 获取字符串的 '[MD5]' 值
 */
fun String.md5Encode(): String {
    val digest = MessageDigest.getInstance("md5")
    val sb = StringBuilder()
    digest.digest(this.toByteArray()).forEach {
        var hexString = (it.toInt() and 0xff).toHexString()
        if (hexString.length < 2) hexString = "0$hexString"
        sb.append(hexString)
    }
    return sb.toString()
}

/**
 * 获取文件MD5值
 *
 * @return MD5字符串
 */
fun File.getMD5(): String {
    try {
        val buffer = ByteArray(8192)
        val md = MessageDigest.getInstance("MD5")
        val fis = FileInputStream(this)
        var len = fis.read(buffer)

        while (len != -1) {
            md.update(buffer, 0, len)
            len = fis.read(buffer)
        }

        fis.close()
        val b = md.digest()

        return Base64.encodeToString(b, Base64.NO_WRAP)
    } catch (e: Exception) {
        return ""
    }
}

/**
 * 转换成json形式的 '[RequestBody]'
 */
fun String.toJsonRequestBody() =
    this.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

/**
 * 使用 '[Gson]' 将 '[Any]' 对象转成Json类型参数的 '[RequestBody]'
 */
fun Any.toJsonRequestBody() = Gson().toJson(this).toJsonRequestBody()

/**
 * 使用 '[Gson]' 将 '[Any]' 对象转换为字符串
 */
fun Any.toJson(): String = Gson().toJson(this)

/**
 * 转换成 '[MultipartBody.Part]'
 */
fun FileInfo.toMultipartBodyPart() = RequestBodyBuilder()
    .addFile(this).buildMultipartBody().part(0)

/**
 * 转换成 '[List]<[MultipartBody.Part]>'
 */
fun List<FileInfo>.toMultipartBodyPart() =
    RequestBodyBuilder().addFiles(this).buildMultipartBody().parts
