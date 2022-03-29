@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.hitcoder.network

import android.util.Log
import com.hitcoder.network.exceptions.ServerException
import com.hitcoder.network.interfaces.FileDownload
import com.hitcoder.network.model.ProgressRequestBody
import com.hitcoder.network.model.ResponseData
import com.hitcoder.network.utils.DownloadFileAsyncTask
import com.hitcoder.network.utils.executeEx
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * 基于[Retrofit]、OkHttp封装的通用网络框架，支持上传、下载进度监听以及自定义拦截器设置
 *
 * 使用步骤:
 *
 * 1.通过 [NetWorkApi.Builder] 构建该工具类对象。
 *
 * 2.网络接口定义的返回值为[Call]
 *
 * 3.调用[getService]+具体接口函数+[executeEx],可以同步执行网络接口，并对结果进行封装，具体见[ResponseData]
 *
 * 4.调用[getRequestProgressService]+具体接口函数+[executeEx],可以同步执行网络接口，观察上传进度并对结果进行封装，具体见[ResponseData]
 *
 * 5.调用[downloadFile]+具体接口函数+[executeEx],可以异步执行网络接口，观察下载进度并对结果进行封装，具体见[ResponseData]
 *
 * 6.使用本依赖的Module可以继续对[Call]进行扩展，调用[executeEx]，传入自定义的exceptionHandler
 *
 */
class NetWorkApi private constructor() {

    /**
     * 基础Url
     */
    var baseUrl: String = ""

    /**
     * 调试模式
     */
    var isDebug = true

    /**
     * 单位：秒 ([TimeUnit.SECONDS])
     */
    var timeout: Long = 30

    /**
     *  [Converter]，默认使用[GsonConverterFactory.create]，子类可根据实际情况覆写该函数
     */
    var converterFactory: Converter.Factory? = GsonConverterFactory.create()

    /**
     * 设置[CallAdapter]
     * 如使用RxJava，则该处应返回RxJavaCallAdapterFactory.create()
     */
    var callAdapterFactory: CallAdapter.Factory? = null

    /**
     * 自定义拦截器
     */
    var interceptor: Interceptor? = null

    /**
     * 子类实现自己的服务器定义的错误机制，通过抛出[ServerException],并定义相应的code与message，
     * 该异常的code与message会封装入接口调用返回结果的[ResponseData]的code与message
     */
    var appErrorHandler: ((Any?) -> Unit)? = null

    private val retrofitHashMap = HashMap<String, Retrofit>()
    private val asyncTaskHashMap = HashMap<String, DownloadFileAsyncTask>()
    private var okHttpClient: OkHttpClient? = null
    private var executors = Executors.newCachedThreadPool()

    /**
     * 创建由定义的API端点的实现service接口,详见[Retrofit.create]
     */
    fun <T> getService(service: Class<T>): T {
        return getRetrofit(service).create(service)
    }

    /**
     * 创建带上传进度回调的，由定义的API端点的实现service接口，详见[Retrofit.create]
     */
    fun <T> getRequestProgressService(
        service: Class<T>,
        listener: (bytesWritten: Long, contentLength: Long, done: Boolean) -> Unit
    ): T {
        return getRetrofitBuilder(getRequestOkHttpClient(listener)).build().create(service)
    }

    /**
     * 文件下载（Get请求）
     *
     * @param fileUrl 文件下载路径
     * @param saveFilePath 文件保存路径(含文件名称)
     * @param fileDownload 文件下载策略及下载进度回调
     * @param contentLength 默认为50M，文件下载接口不返回contentLength的时候，使用该长度，可以从外部传入文件大小，用以正确展示文件下载进度
     * @return [DownloadFileAsyncTask]对象，可设置[DownloadFileAsyncTask.tag]标签,调用[canDownloadByTag]函数取消下载线程
     */
    fun downloadFile(
        fileUrl: String,
        saveFilePath: String,
        fileDownload: FileDownload? = null,
        contentLength: Long = 50 * 1024 * 1024
    ) =
        DownloadFileAsyncTask(
            fileUrl,
            saveFilePath,
            fileDownload,
            contentLength,
            ::getUrlConnection
        ).apply {
            asyncTaskHashMap[fileUrl] = this
            executeOnExecutor(executors)
        }

    fun canDownloadByTag(tag: Any) {
        asyncTaskHashMap.forEach {
            if (it.value.getTag() == tag) it.value.stopTask()
        }
    }


    /**
     * 获取[Retrofit]的实例，如实例为空，则根据配置进行创建
     */
    fun <T> getRetrofit(service: Class<T>): Retrofit {
        var retrofit = retrofitHashMap[baseUrl + service.name]

        if (retrofit != null) {
            return retrofit
        }

        retrofit = getRetrofitBuilder(getOkHttpClient()).build()

        retrofitHashMap[baseUrl + service.name] = retrofit

        return retrofit
    }

    /**
     * 获取[OkHttpClient]的实例，如实例为空，则根据配置进行创建
     */
    fun getOkHttpClient(): OkHttpClient {
        if (okHttpClient == null) {
            okHttpClient = getOkHttpClientBuilder().apply {
                //普通请求在DEBUG模式下添加日志打印
                if (isDebug) {
                    addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
                }
            }.build()
        }
        return okHttpClient!!
    }

    /**
     * 获取[OkHttpClient]的实例，如实例为空，则根据配置进行创建
     */
    private fun getRequestOkHttpClient(listener: (bytesWritten: Long, contentLength: Long, done: Boolean) -> Unit) =
        getOkHttpClientBuilder().run {
            //添加上传进度拦截器
            addInterceptor {
                it.proceed(
                    it.request().newBuilder()
                        .method(
                            it.request().method,
                            ProgressRequestBody(it.request().body, listener)
                        )
                        .build()
                )
            }
            build()
        }

    /**
     * 获取通用[Retrofit.Builder]对象
     */
    private fun getRetrofitBuilder(okHttpClient: OkHttpClient) = Retrofit.Builder().apply {

        baseUrl(baseUrl)

        client(okHttpClient)

        converterFactory?.let {
            addConverterFactory(it)
        }

        callAdapterFactory?.let {
            addCallAdapterFactory(it)
        }
    }


    /**
     * 获取通用[OkHttpClient.Builder]对象
     */
    private fun getOkHttpClientBuilder() = OkHttpClient.Builder().apply {

        connectTimeout(timeout, TimeUnit.SECONDS)
        writeTimeout(timeout, TimeUnit.SECONDS)
        readTimeout(timeout, TimeUnit.SECONDS)

        interceptor?.let { addInterceptor(it) }

        //增加拦截器，在入参header中增加操作系统和版本号，并打印网络请求执行时间
        addInterceptor {
            val requestTime = System.currentTimeMillis()

            val response = it.proceed(
                it.request().newBuilder()
                    .apply {
                        addHeader("os", "android")
                    }.build()
            )

            if (isDebug)
                Log.d(
                    this@NetWorkApi.javaClass.simpleName,
                    "requestTime=" + (System.currentTimeMillis() - requestTime)
                )

            response
        }
    }

    /**
     * 获取Get请求Url连接，编码格式为"UTF-8"
     * @param url 请求地址
     */
    private fun getUrlConnection(url: String): HttpURLConnection {
        val urlConnection = URL(url).openConnection() as HttpURLConnection
        urlConnection.connectTimeout = (timeout * 1000).toInt()
        urlConnection.readTimeout = (timeout * 1000).toInt()
        urlConnection.useCaches = true
        urlConnection.requestMethod = "GET"
        //设置客户端与服务连接类型
        urlConnection.addRequestProperty("Connection", "Keep-Alive")
        urlConnection.addRequestProperty("Charset", "UTF-8")
        urlConnection.addRequestProperty("os", "android")
        return urlConnection
    }

    /**
     * 构造器
     */
    object Builder {

        /**
         * 基础Url
         */
        private var baseUrl: String = ""

        /**
         * 自定义错误处理，通过抛出[ServerException],并定义相应的code与message，
         * 该异常的code与message会封装入接口调用返回结果的[ResponseData]的code与message
         */
        private var appErrorHandler: ((Any?) -> Unit)? = null

        /**
         * 单位：秒 ([TimeUnit.SECONDS])
         */
        private var timeout: Long = 30

        /**
         * 调试模式
         */
        private var isDebug = true

        /**
         *  [Converter]，默认使用[GsonConverterFactory.create]，子类可根据实际情况覆写该函数
         */
        private var converterFactory: Converter.Factory? = GsonConverterFactory.create()

        /**
         * 设置[CallAdapter]
         * 如使用RxJava，则该处应返回RxJavaCallAdapterFactory.create()
         */
        private var callAdapterFactory: CallAdapter.Factory? = null

        /**
         * 自定义拦截器
         */
        private var interceptor: Interceptor? = null

        /**
         * 设置 [Retrofit] 的BaseUrl
         */
        fun setBaseUrl(baseUrl: String): Builder {
            this.baseUrl = baseUrl
            return this
        }

        /**
         * 自定义错误处理，通过抛出[ServerException],并定义相应的code与message，
         * 该异常的code与message会封装入接口调用返回结果的[ResponseData]的code与message
         */
        fun setErrorHandler(handler: ((Any?) -> Unit)?): Builder {
            appErrorHandler = handler
            return this
        }

        /**
         * 设置超时时间
         */
        fun setTimeout(timeout: Long): Builder {
            this.timeout = timeout
            return this
        }

        /**
         * 设置调试模式，调试模式时，访问网络接口会打印日志
         */
        fun setDebug(isDebug: Boolean): Builder {
            this.isDebug = isDebug
            return this
        }

        /**
         *  [Converter]，默认使用[GsonConverterFactory.create]
         */
        fun setConverterFactory(factory: Converter.Factory?): Builder {
            this.converterFactory = factory
            return this
        }

        /**
         * 设置[CallAdapter]
         * 如使用RxJava，则该处应返回RxJavaCallAdapterFactory.create()
         */
        fun setCallAdapterFactory(factory: CallAdapter.Factory?): Builder {
            this.callAdapterFactory = factory
            return this
        }

        /**
         * 添加自定义拦截器
         */
        fun addInterceptor(interceptor: Interceptor): Builder {
            this.interceptor = interceptor
            return this
        }

        fun build(): NetWorkApi {
            return NetWorkApi().apply {
                baseUrl = this@Builder.baseUrl
                isDebug = this@Builder.isDebug
                timeout = this@Builder.timeout
                converterFactory = this@Builder.converterFactory
                callAdapterFactory = this@Builder.callAdapterFactory
                interceptor = this@Builder.interceptor
                appErrorHandler = this@Builder.appErrorHandler
            }
        }

    }

}

