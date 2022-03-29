package com.libwriting.dao

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.libwriting.giftools.GifMake
import com.libwriting.service.UploadService
import com.libwriting.ui.PointEx
import com.libwriting.utils.DaoHelper
import com.loopj.android.http.TextHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.regex.Matcher
import java.util.regex.Pattern

class Article: DaoBase() {
    var id: String = ""
    var userId: String = ""
    var type: Int = poetryType
    var degree: Int = 3
    var title: String = ""
    var ttf: String = TtfType.Kai.name
    var points: String = ""
    var cover: String = ""
    var video: String = ""
    var localVideo: String = ""
    var cols: Int = 0
    var rows: Int = 0
    var gridWidth: Float = 0f
    var gridHeight: Float = 0f
    var gridType: Int=1

    fun fromJson(jsonObj: JSONObject): Boolean {
        if (jsonObj != null) {
            try {
                id = jsonObj.getString("id")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                userId = jsonObj.getString("userId")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                title = jsonObj.getString("title")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                type = jsonObj.getInt("type")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                degree = jsonObj.getInt("degree")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                ttf = jsonObj.getString("tff")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                points = jsonObj.getString("points")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                cover = jsonObj.getString("cover")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                video = jsonObj.getString("writeVideo")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                gridType = jsonObj.getInt("gridType")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                gridWidth = jsonObj.getDouble("gridWidth").toFloat()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                gridHeight = jsonObj.getDouble("gridHeight").toFloat()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                cols = jsonObj.getInt("cols")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                rows = jsonObj.getInt("rows")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }
    private fun makeMd5FileName(ext: String): String {
        var fileName: String = "${userId}${type}${ttf}.${ext}"
        if(points.isNotEmpty()) {
            fileName = ""
            var md5 = MessageDigest.getInstance("MD5")
            md5.update(points.toByteArray())
            var ba = md5.digest()
            ba.forEach {
                fileName += (it.toUInt() and 255u).toString(radix=16)
            }
            fileName += ".$ext"
        }
        return fileName
    }

    fun putPic(context: Context, bm: Bitmap): Boolean {
        try {
            var fileName = makeMd5FileName("png")
            var file = File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
            file.deleteOnExit()
            file.createNewFile()
            val os: OutputStream = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 100, os)
            os.flush()
            os.close()
            cover = fileName
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }
    /*
        向后台提交数据
        此函数采用http方式提交文件
        当文件多时可能会引起web端口带宽问题
     */
    override fun submit(context: Context?, resp: OnSubmitResponse, taskId: Int?) {
        if(video != null && video.isNotEmpty()) {
            var file = File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path + "/" + video)
            DaoHelper.run { uploadFileVideo(file, object: TextHttpResponseHandler() {
                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    responseString: String?
                ) {
                    try {
                        var jsonObj = JSONObject(responseString)
                        var code = jsonObj.getInt("code")
                        if (code == 200) {

                        }
                    }catch (e: Exception) {

                    }
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    responseString: String?,
                    throwable: Throwable?
                ) {
                }

            }) }
        }

        var file = File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path + "/" + cover)
        DaoHelper.run { uploadFilePic(file, object: TextHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseString: String?
            ) {
                try {
                    var jsonObj = JSONObject(responseString)
                    var code = jsonObj.getInt("code")
                    if(code == 200) {
                        DaoHelper.run {
                            insertArticle(this@Article, InsertResponse().apply {
                                this.onSubmitCallback = resp
                            })
                        }
                    } else {
                        var msg = jsonObj.getString("message")
                        resp?.onFailure("error:${msg}", taskId)
                    }
                } catch (e: Exception) {
                    resp?.onFailure(e.message, taskId)
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseString: String?,
                throwable: Throwable?
            ) {
                resp?.onFailure(throwable?.message, taskId)
            }
        })}

    }

    override fun submit(context: Context?, uploadService: UploadService, resp: OnSubmitResponse, taskId: Int?) {

    }
}