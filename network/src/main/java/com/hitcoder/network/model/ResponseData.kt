package com.hitcoder.network.model

/**
 * 网络请求封装数据类
 * @param code 结果码，[NetworkError.SUCCESS.code]为成功
 * @param message 错误信息，网络请求不成功时返回文字描述
 * @param data 网络请求服务器返回结果解析的数据类
 */
data class ResponseData<T>(val code: Int, val message: String, val data: T?) {
    /**
     * 是否成功
     */
    fun isSuccess() = code == NetworkError.SUCCESS.code
}
