package com.hitcoder.network.model

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*

/**
 * 自定义上传进度请求体，用来进行整个网络请求参数上传进度的监听
 * @param requestBody 要被包装的原[RequestBody]对象
 * @param listener 上传进度回调对象
 */
class ProgressRequestBody(
    private val requestBody: RequestBody?,
    private val listener: (bytesRead: Long, contentLength: Long, done: Boolean) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? {
        return requestBody?.contentType()
    }

    override fun contentLength(): Long {
        return requestBody?.contentLength() ?: -1
    }

    /**
     * 写入，回调进度接口
     */
    override fun writeTo(sink: BufferedSink) {
        val forwardingSink = object : ForwardingSink(sink) {
            //当前写入字节数
            private var bytesWritten = 0L

            //总字节长度，避免多次调用contentLength()方法
            private var contentLength = 0L

            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                if (contentLength == 0L) {
                    //获得contentLength的值，后续不再调用
                    contentLength = contentLength()
                }
                //增加当前写入的字节数
                bytesWritten += byteCount

                //主线程回调，如果contentLength()不知道长度，会返回-1
                Handler(Looper.getMainLooper()).post {
                    listener.invoke(
                        bytesWritten,
                        contentLength,
                        bytesWritten == contentLength
                    )
                }
            }
        }
        val buffer = forwardingSink.buffer()
        requestBody?.writeTo(buffer)
        buffer.flush()
    }

}