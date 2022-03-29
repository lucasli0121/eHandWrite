@file:Suppress("unused")

package com.hitcoder.network.utils

import android.os.AsyncTask
import com.hitcoder.network.interfaces.FileDownload
import com.hitcoder.network.model.Cover
import com.hitcoder.network.model.NetworkError
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.UnknownHostException

/**
 * 文件下载异步任务封装类
 *
 * 文件下载策略见[Cover]
 * @param fileUrl 文件下载路径
 * @param saveFilePath 文件保存路径(含文件名称)
 * @param fileDownload 文件下载策略及下载进度回调
 * @param contentLength 默认为50M，应对文件下载接口不返回contentLength的情况，可以从外部传入文件大小，用以正确展示文件下载进度
 */
class DownloadFileAsyncTask(
    private val fileUrl: String,
    private val saveFilePath: String,
    private val fileDownload: FileDownload?,
    private val contentLength: Long = 50 * 1024 * 1024,
    private val getConnection: (String) -> HttpURLConnection
) : AsyncTask<Void, DownloadFileAsyncTask.Progress, DownloadFileAsyncTask.CheckUpdateResult>() {

    private var fileName: String
    private val fileDir: String
    private lateinit var mFile: File

    private var tag: Any? = null

    init {
        fileName = saveFilePath.substring(saveFilePath.lastIndexOf(File.separator))
        fileDir = saveFilePath.substring(0, saveFilePath.lastIndexOf(File.separator))
    }

    data class CheckUpdateResult(var resultCode: NetworkError, var errorMessage: String)

    data class Progress(val writeBytes: Long, val totalLength: Long)

    fun tag(tag: Any): DownloadFileAsyncTask {
        this.tag = tag
        return this
    }

    fun getTag() = this.tag

    fun stopTask() {
        if (Status.RUNNING == this.status) cancel(true)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        fileDownload?.downloadStart()
    }

    override fun doInBackground(vararg p0: Void?): CheckUpdateResult {
        var randomCacheFile: RandomAccessFile? = null

        try {
            //下载文件
            val name: String = fileName.substring(0, fileName.lastIndexOf("."))
            val type: String = fileName.substring(fileName.lastIndexOf(".") + 1)

            //获取本地保存的升级文件
            mFile = File(saveFilePath)

            if (fileDownload?.cover() == Cover.IF_EXIT && mFile.exists()) {
                //直接返回下载完成
                return CheckUpdateResult(NetworkError.SUCCESS, "")
            }

            //获取缓存文件
            val cacheFile = File(fileDir, fileUrl.md5Encode())

            if (!cacheFile.exists()) cacheFile.createNewFile()
            if (!cacheFile.exists()) throw Exception(ERROR_MSG_CREATE_FILE_FAIL)

            randomCacheFile = RandomAccessFile(cacheFile, "rw")

            //1.判断服务器是否支持断点续传
            var urlConnection = getConnection(fileUrl)
            /**
             * 设置Range，下载范围
             * 表示为头500个字节：Range: bytes=0-499
             * 表示第二个500字节：Range: bytes=500-999
             * 表示最后500个字节：Range: bytes=-500
             * 表示500字节以后的范围：Range: bytes=500-
             * 第一个和最后一个字节：Range: bytes=0-0,-1
             * 同时指定几个范围：Range: bytes=500-600,601-999
             */
            urlConnection.setRequestProperty("Range", "bytes=0-499")
            urlConnection.connect()

            if (isCancelled) throw Exception(ERROR_MSG_DOWNLOAD_CANCEL)

            //断点下载返回的结果码是 206
            if (urlConnection.responseCode != HttpURLConnection.HTTP_OK &&
                urlConnection.responseCode != HttpURLConnection.HTTP_PARTIAL
            ) {
                throw Exception(ERROR_MSG_NET_ERROR)
            }

            //判断是否支持断点续传
            val continuing = urlConnection.contentLength == 500
            urlConnection.disconnect()

            //开始文件下载
            urlConnection = getConnection(fileUrl)

            //根据判断结果，准备文件下载相应信息
            var readLength: Long = 0
            if (continuing) {
                //将缓存文件长度设置为已读长度
                readLength = randomCacheFile.length()
                //设置写文件起始位置
                randomCacheFile.seek(randomCacheFile.length())
                // 设置Range，下载范围
                urlConnection.setRequestProperty("Range", "bytes=${randomCacheFile.length()}-")
            }
            urlConnection.connect()

            if (isCancelled) throw Exception(ERROR_MSG_DOWNLOAD_CANCEL)

            //断点下载返回的结果码是 206
            if (urlConnection.responseCode != HttpURLConnection.HTTP_OK &&
                urlConnection.responseCode != HttpURLConnection.HTTP_PARTIAL
            ) {
                throw Exception(ERROR_MSG_NET_ERROR)
            }

            val inputStream = urlConnection.inputStream

            //以防万一，如果服务器接口无法获取contentLength，设置一个50M的长度
            val fileLength = if (urlConnection.contentLength.toLong() == -1L) {
                contentLength
            } else {
                urlConnection.contentLength.toLong() + randomCacheFile.length()
            }

            val buf = ByteArray(4096)
            var length = inputStream.read(buf)

            var memoryProgress = 0
            while (length != -1) {

                if (isCancelled) throw Exception(ERROR_MSG_DOWNLOAD_CANCEL)

                randomCacheFile.write(buf, 0, length)
                readLength += length.toLong()
                val curProgress = (readLength.toFloat() / fileLength * 100).toInt()
                if (curProgress > memoryProgress) {
                    memoryProgress = curProgress
                    publishProgress(Progress(readLength, fileLength))
                }
                length = inputStream.read(buf)
            }

            //如果下载策略为：文件名称冲突时，重命名
            if (fileDownload?.cover() == Cover.RENAME) {
                var i = 1
                while (mFile.exists()) {
                    fileName = name + "(" + i++ + ")." + type
                    mFile = File(fileDir, fileName)
                }
            }

            //将缓存文件更名为升级文件
            mFile.createNewFile()
            cacheFile.renameTo(mFile)

            //下载成功
            return CheckUpdateResult(NetworkError.SUCCESS, "")

        } catch (e: UnknownHostException) {
            return CheckUpdateResult(NetworkError.NETWORD_ERROR, ERROR_MSG_CONNECT_ERROR)
        } catch (e: ConnectException) {
            return CheckUpdateResult(NetworkError.NETWORD_ERROR, ERROR_MSG_CONNECT_ERROR)
        } catch (e: IOException) {
            //捕获异常，返回下载失败
            return CheckUpdateResult(NetworkError.FILE_ERROR, ERROR_MSG_DOWNLOAD_FAIL)
        } catch (e: Exception) {
            val message = e.message

            val resultMsg = when {
                message == null -> {
                    ERROR_MSG_DOWNLOAD_FAIL
                }
                message.isNotEmpty() -> {
                    message
                }
                else -> {
                    ERROR_MSG_DOWNLOAD_FAIL
                }
            }

            return CheckUpdateResult(NetworkError.FILE_ERROR, resultMsg)
        } finally {
            randomCacheFile?.close()
        }
    }

    override fun onPostExecute(result: CheckUpdateResult) {
        super.onPostExecute(result)
        if (result.resultCode == NetworkError.SUCCESS) {
            fileDownload?.downloadSuccess(mFile.absolutePath)
        } else {
            fileDownload?.downloadFail(result.errorMessage)
        }
    }

    override fun onCancelled() {
        super.onCancelled()
        fileDownload?.downloadCancel()
    }

    override fun onProgressUpdate(vararg values: Progress?) {
        super.onProgressUpdate(*values)
        values[0]?.let {
            fileDownload?.downloadInProgress(it.writeBytes, it.totalLength)
        }
    }

}