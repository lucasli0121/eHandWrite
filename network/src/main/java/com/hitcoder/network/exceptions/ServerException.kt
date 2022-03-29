package com.hitcoder.network.exceptions

import java.lang.RuntimeException

/**
 * 用户自己抛出的异常，可对于不同的服务器的错误情况进行封装
 */
class ServerException(var code: Int, override var message: String) : RuntimeException()