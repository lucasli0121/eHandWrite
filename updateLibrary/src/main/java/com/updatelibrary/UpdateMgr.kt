package com.updatelibrary

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import com.updatelibrary.XmlParser.parseXml
import com.loopj.android.http.TextHttpResponseHandler
import android.widget.TextView
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.ProgressBar
import androidx.core.content.FileProvider
import com.updatalibrary.R
import cz.msebera.android.httpclient.Header
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.HashMap

class UpdateMgr(private val mContext: Context) : TextHttpResponseHandler() {
    private var appName: String? = null
    private var url: String? = null
    private var progress = 0
    private var mProgress: ProgressBar? = null
    private var _labelTxt: TextView? = null
    private var mDownloadDialog: Dialog? = null
    private var _cancel = false
    private var _savePath: String? = null
    var packName: String = ""
    var serverUri = ""
    var checkListener: UpdateCheckListener? = null
    companion object {
        private const val DOWNLOAD = 1
        private const val DOWNLOAD_FINISH = 2
        private const val DOWNLOAD_CLOSE = 3
    }
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                DOWNLOAD -> {
                    if (mProgress != null) {
                        mProgress!!.progress = progress
                    }
                    if (_labelTxt != null) {
                        val txt = String.format("%d%%", progress)
                        _labelTxt!!.text = txt
                    }
                }
                DOWNLOAD_FINISH ->
                    installApk()
                DOWNLOAD_CLOSE -> if (mDownloadDialog != null) {
                    mDownloadDialog!!.dismiss()
                }
                else -> {
                }
            }
        }
    }

    fun checkUpdate() {
        HttpClientUtil[serverUri + "version.xml", null, this]
    }

    private fun getVersionCode(context: Context): Long {
        var versionCode = 0L
        try {
            versionCode = context.packageManager.getPackageInfo(packName, 0).longVersionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return versionCode
    }

    interface UpdateCheckListener {
        fun getId(): String
    }
    private fun isUpdate(xmlResponse: String): Boolean {
        val versionCode = getVersionCode(mContext)
        var hashMap: HashMap<String, HashMap<String, String>>? = null
        try {
            val inStream: InputStream = ByteArrayInputStream(xmlResponse.toByteArray())
            hashMap = parseXml(inStream)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (null != hashMap) {
            var id: String? = null
            var version: String? = null
            if(checkListener != null) {
                id = checkListener?.getId()
                if(id != null) {
                    hashMap!!.forEach { (t, u) ->
                        if(t == id) {
                            version = u["version"]
                            appName = u["app"]
                            url = u["url"]
                            return@forEach
                        }
                    }
                }
            }
            if(version != null && version?.isNotEmpty() == true) {
                val serviceCode = Integer.valueOf(version)
                if (serviceCode > versionCode) {
                    return true
                }
            }
        }
        return false
    }

    private fun showNoticeDialog() {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle(R.string.app_update_title)
        builder.setMessage(R.string.app_update_info)
        builder.setPositiveButton(
            R.string.app_update_ok,
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                showDownloadDialog()
            })
        // �Ժ����
        builder.setNegativeButton(
            R.string.app_update_cancel,
            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        val noticeDialog: Dialog = builder.create()
        noticeDialog.show()
    }

    private fun showDownloadDialog() {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle(R.string.app_updating)
        val inflater = LayoutInflater.from(mContext)
        val v: View = inflater.inflate(R.layout.app_update_progress, null)
        mProgress = v.findViewById<View>(R.id.update_progress) as ProgressBar
        mProgress!!.max = 100
        _labelTxt = v.findViewById<View>(R.id.label) as TextView
        builder.setView(v)
        builder.setNegativeButton(R.string.cancel) { dialog, which ->
            dialog.dismiss()
            _cancel = true
        }
        mDownloadDialog = builder.create()
        (mDownloadDialog as AlertDialog)?.show()
        downloadApk()
    }

    private fun downloadApk() {
        downloadApkThread().start()
    }

    private inner class downloadApkThread : Thread() {
        override fun run() {
            try {
                _savePath = mContext?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
//                    val sdpath = Environment.getExternalStorageDirectory().toString() + "/"
//                    _savePath = sdpath + "download"
                if(appName != null && url != null) {
                    val url = URL(url)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connect()
                    val length = conn.contentLength
                    val `is` = conn.inputStream
                    val file = File(_savePath)
                    if (!file.exists()) {
                        file.mkdir()
                    }

                    val apkFile = File(_savePath, appName)
                    val fos = FileOutputStream(apkFile)
                    var count = 0
                    val buf = ByteArray(1024)
                    do {
                        val numread = `is`.read(buf)
                        count += numread
                        progress = (count.toFloat() / length * 100).toInt()
                        mHandler.sendEmptyMessage(DOWNLOAD)
                        if (numread <= 0) {
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH)
                            break
                        }
                        fos.write(buf, 0, numread)
                    } while (!_cancel)
                    fos.close()
                    `is`.close()
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mHandler.sendEmptyMessage(DOWNLOAD_CLOSE)
        }
    }

    /**
     */
    private fun installApk() {
        if(appName != null) {
            val apkfile = File(_savePath, appName)
            if (!apkfile.exists()) {
                return
            }
            val i = Intent(Intent.ACTION_VIEW)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val fileUri: Uri = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                Uri.fromFile(apkfile)
            } else {
                FileProvider.getUriForFile(
                    mContext,
                    mContext.applicationContext.packageName + ".provider",
                    apkfile
                )
            }
            i.setDataAndType(fileUri, "application/vnd.android.package-archive")
            mContext.startActivity(i)
        }
    }

    override fun onFailure(arg0: Int, arg1: Array<Header?>?, arg2: String?, arg3: Throwable?) {
    }

    override fun onSuccess(statusCode: Int, headers: Array<Header?>?, responseString: String) {
        if (isUpdate(responseString)) {
            showNoticeDialog()
        }
    }
}