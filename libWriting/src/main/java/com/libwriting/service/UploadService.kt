package com.libwriting.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.libwriting.utils.Ftp
import java.io.File

class UploadService : Service() {
    private val Tag = "UploadService"
    private val ftpUri = ""
    private val ftpPort = 21
    private val userName = ""
    private val userPwd = ""
    var uploadCallback: UploadCallbackInterface? = null
    fun startUpload(localFullName: String, remoteFileName: String, remotePath: String, context: Context?, taskId: Int?) {
        Log.i(Tag, "startUpload()")
        val file = File(localFullName)
        val filelen = file.length()
        var res = Ftp.uploadFile(
            ftpUri,
            ftpPort,
            userName,
            userPwd,
            remotePath,
            remoteFileName,
            localFullName,
            object : Ftp.OnTranslateListener {
                override fun OnTranslate(totalBytes: Long, transBytes: Long) {
                    if (uploadCallback != null) {
                        uploadCallback!!.onUpload(remoteFileName, context, filelen, totalBytes, taskId)
                    }
                }
            })
        if (!res) {
            if (uploadCallback != null) {
                uploadCallback!!.onFailed(context,"上传 失败", taskId)
            }
        }
    }

    interface UploadCallbackInterface {
        fun onUpload(fileName: String?, context: Context?, total: Long, `val`: Long, taskId: Int?)
        fun onFailed(context: Context?, reason: String?, taskId: Int?)
    }

    override fun onBind(intent: Intent): IBinder? {
        return UploadBinder()
    }

    private inner class UploadBinder : Binder() {
        val service: UploadService
            get() = this@UploadService
    }

    class UploadServiceConnect : ServiceConnection{
        var upService: UploadService? = null
        var upCallback: UploadCallbackInterface? = null
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            upService = (service as UploadService.UploadBinder).service
            upService?.uploadCallback = upCallback
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }

    }
}