package com.hitcoder.network.model

/**
 * 封装的网络请求结果码枚举类
 */
enum class NetworkError(val code: Int) {
    /**
     * 成功
     */
    SUCCESS(20000),

    /**
     * 未知错误
     */
    UNKNOWN(10000),

    /**
     * 取消网络接口访问
     */
    CANCEL(10001),

    /**
     * 解析错误
     */
    PARSE_ERROR(10002),

    /**
     * 网络错误
     */
    NETWORD_ERROR(10003),

    /**
     * 协议出错
     */
    HTTP_ERROR(10004),

    /**
     * 证书出错
     */
    SSL_ERROR(10005),

    /**
     * 连接超时
     */
    TIMEOUT_ERROR(10006),

    /**
     * 文件操作失败
     */
    FILE_ERROR(10007)
    
}