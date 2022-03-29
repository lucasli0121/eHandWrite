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

class TrackPoint: DaoBase() {
    enum class Type(s: String) {
        Word("Word"),    // 单字
        Article("Article")  // 文章
    }

    lateinit var id: String
    var userId: String = ""
    lateinit var wordId: String
    var wordText: String? = null
    var type: String = Type.Word.name
    var degree: Int = 10
    lateinit var ttf: String
    lateinit var points: String
    var outline: String = ""
    var pic: String = ""
    var video: String = ""
    var localVideo: String = ""
    var stroke: String = ""
    var voice: String = ""
    var voiceFileName: String = ""
    var realVoiceFile: String = ""
    var gifMake: GifMake? = null
     //新增字段
    var fontclr:String="000000"
    var bkclr:String="ffffff"
    var gridType: Int=1
    var lineclr:String="ffffff"
    var width: Int = 0
    var height: Int = 0

    // 拷贝一份副本
    fun copy(): TrackPoint {
        return TrackPoint().apply {
            this.id = id
            this.userId = userId
            this.wordId = wordId
            this.type = type
            this.degree = degree
            this.ttf = ttf
            this.points = points
            this.outline = outline
            this.pic = pic
            this.video = video
            this.fontclr=fontclr
            this.bkclr=bkclr
            this.gridType=gridType
            this.lineclr=lineclr
            this.width = 0
            this.height = 0
        }
    }
    fun fromJson(jsonObj: JSONObject): Boolean {
        if (jsonObj != null) {
            try {
                id = jsonObj.getString("id")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                wordId = jsonObj.getString("wordId")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                type = jsonObj.getString("type")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                degree = jsonObj.getInt("degree")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                ttf = jsonObj.getString("ttf")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                points = jsonObj.getString("points")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                outline = jsonObj.getString("outline")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                pic = jsonObj.getString("pic")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                video = jsonObj.getString("video")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                fontclr = jsonObj.getString("fontclr")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                bkclr = jsonObj.getString("bkclr")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                gridType = jsonObj.getInt("gridType")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                lineclr = jsonObj.getString("lineclr")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }
    private fun makeMd5FileName(ext: String): String {
        var fileName: String = "${wordId}${type}${ttf}.${ext}"
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
    fun makeGifName(): String {
        video = makeMd5FileName("gif")
        return video
    }
    fun putPic(context: Context, bm: Bitmap): Boolean {
        var fileName = makeMd5FileName("png")
        var file = File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
        file.deleteOnExit()
        file.createNewFile()
        val os: OutputStream = FileOutputStream(file)
        bm.compress(Bitmap.CompressFormat.PNG, 100, os)
        os.flush()
        os.close()
        pic = fileName
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
        if(voiceFileName != null && voiceFileName.isNotEmpty()) {
            var file =
                File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path + "/" + voiceFileName)
            DaoHelper.run {
                uploadFileVoice(file, object : TextHttpResponseHandler() {
                    override fun onSuccess(
                        statusCode: Int,
                        headers: Array<out Header>?,
                        responseString: String?
                    ) {
                    }

                    override fun onFailure(
                        statusCode: Int,
                        headers: Array<out Header>?,
                        responseString: String?,
                        throwable: Throwable?
                    ) {
                    }


                })
            }
        }

        var file = File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path + "/" + pic)
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
                            insertPoints(this@TrackPoint, TrackPointResponse().apply {
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
    /*
        向后台提交数据
        此函数采用ftp方式，不会引起web端口压力问题
     */
    override fun submit(context: Context?, uploadService: UploadService, resp: OnSubmitResponse, taskId: Int?) {
        System.out.println("dddddddddd-----------")
        var picFile = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path + "/" + pic
        uploadService?.startUpload(picFile, pic, "picture", context, taskId)
        if(this.video != null && this.video.isNotEmpty()) {
            uploadService?.startUpload(localVideo, video, "video", context, taskId)
        }
//        gifMake?.let{
//            video = makeGifName()
//            var videoFile = it.makeGifFile(video).toString()
//            if(videoFile != null) {
//                taskId?.apply { this + 1 }
//                uploadService?.startUpload(videoFile, video, "video", context, taskId)
//            }
//        }
        if(voiceFileName != null && realVoiceFile != null && voiceFileName.isNotEmpty() && realVoiceFile.isNotEmpty()) {
            uploadService?.startUpload(realVoiceFile, voiceFileName, "voice", context, taskId)
        }
        Handler(Looper.getMainLooper()).post{DaoHelper.run {
            insertPoints(this@TrackPoint, TrackPointResponse().apply {
                this.onSubmitCallback = resp
                this.taskId = taskId
            })}
        }
    }
    // 内部类，用于提交采集数据后的回应结果
    // 根据后台回应结果，进行UI的不同显示
    private inner class TrackPointResponse: TextHttpResponseHandler() {
        var onSubmitCallback: OnSubmitResponse? = null
        var taskId: Int? = null
        override fun onSuccess(status: Int, p1: Array<out Header>?, jsonRaw: String?) {
            try {
                var jsonObj = JSONObject(jsonRaw)
                var code = jsonObj.getInt("code")
                if(code == 20000) {
                    //Tost
                    onSubmitCallback?.onSuccess(200, "提交采集数据成功", taskId)
                } else {
                    var msg = jsonObj.getString("data")
                    onSubmitCallback?.onFailure("error:${msg}", taskId)
                }
            }catch (e: Exception) {
                onSubmitCallback?.onFailure(e.message, taskId)
            }
        }

        override fun onFailure(i: Int, p1: Array<out Header>?, s: String?, error: Throwable?) {
            onSubmitCallback?.onFailure(error?.message, taskId)
        }
    }
}