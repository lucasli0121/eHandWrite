package com.libwriting.utils

import com.libwriting.dao.*
import com.loopj.android.http.BaseJsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.loopj.android.http.TextHttpResponseHandler
import java.io.File

object DaoHelper {
    private const val TAG = "DaoHelper"

    private const val uploadUri = ""
    private const val baseUri = ""

    /*
		获取所有年纪
	 */
    fun refreshGrade(jsonHttpResponse: BaseJsonHttpResponseHandler<ArrayList<Grade>>) {
        val url = "$baseUri/grade/dropDownGrade"
        val params = RequestParams()
        HttpClientUtil[url, params, jsonHttpResponse]
       // HttpClientUtil.post(url, params, jsonHttpResponse)
    }

    fun queryWord(userId: String, gradeId: String, pageSize: Int, pageNum: Int, jsonHttpResponse: BaseJsonHttpResponseHandler<ArrayList<Word>>) {
        val url = "$baseUri/word/getWordByGradeId"
        val params = RequestParams()
        params.put("userId", userId)
        if (gradeId != null) {
            params.put("gradeId", gradeId)
        }
        params.put("pageSize", pageSize)
        params.put("pageNum", pageNum)
        HttpClientUtil[url, params, jsonHttpResponse]
    }

   /* fun updateWord(word: Word, jsonHttpResponse: TextHttpResponseHandler) {
        val url = "$baseUri/word/update"
        val params = RequestParams()
        params.put("ID", word.ID)
        params.put("GradeID", word.GradeID)
        params.put("text", word.text)
        params.put("desc", word.desc)
        HttpClientUtil.post(url, params, jsonHttpResponse)
    }*/

    fun insertPoints(p: TrackPoint, jsonHttpResponse: TextHttpResponseHandler) {
        val url = "$baseUri/point/addWordPoint"
        val params = RequestParams()
        params.put("userId", p.userId)
        params.put("wordId", p.wordId) //1
        params.put("type", p.type)  //1
        params.put("degree", p.degree) //1
        params.put("ttf", p.ttf) //1
        params.put("points", p.points) //1
        params.put("outline", p.outline) //1
        params.put("pic", p.pic) //1
        params.put("strokeOne", p.stroke) //2
        params.put("voice", p.voice) //1
//        params.put("fontclr",p.fontclr)
//        params.put("bkclr",p.bkclr)
//        params.put("gridType",p.gridType)
//        params.put("lineclr",p.lineclr)
        params.put("width", p.width)
        params.put("height", p.height)
        params.setUseJsonStreamer(true)
        HttpClientUtil.addHeader("contentType", "application/json;charset=UTF-8")
        HttpClientUtil.post(url, params, "application/json;charset=UTF-8", jsonHttpResponse)

    }

    fun deletePoints(traceId: String, jsonHttpResponse: TextHttpResponseHandler) {
        val url = "$baseUri/point/deletePointById/${traceId}"
        HttpClientUtil.removeAllHeader()
        HttpClientUtil.delete(url, jsonHttpResponse)
    }

    /*
        插入整篇写内容
     */
    fun insertArticle(obj: Article, jsonHttpResponse: TextHttpResponseHandler) {
        val url = "$baseUri/poetry/addPoetry"
        val params = RequestParams()
        params.put("cols", obj.cols)
        params.put("rows", obj.rows)
        params.put("cover", obj.cover)
        params.put("degree", obj.degree)
        params.put("gridHeight", obj.gridHeight)
        params.put("gridWidth", obj.gridWidth)
        params.put("gridType", obj.gridType)
        params.put("points", obj.points)
        params.put("tff", obj.ttf)
        params.put("title", obj.title)
        params.put("type", obj.type)
        params.put("userId", obj.userId)
        params.put("writeVideo", obj.video)
        params.setUseJsonStreamer(true)
        HttpClientUtil.addHeader("contentType", "application/json;charset=UTF-8")
        HttpClientUtil.post(url, params, "application/json;charset=UTF-8", jsonHttpResponse)
    }
    fun uploadFilePic(file: File, jsonHttpResponse: TextHttpResponseHandler) {
        val url = "$uploadUri/upload/picture"
        val params = RequestParams()
        params.put("file", file)
        HttpClientUtil.removeAllHeader()
        HttpClientUtil.post(url, params, jsonHttpResponse)
    }

    fun uploadFileVideo(file: File, jsonHttpResponse: TextHttpResponseHandler) {
        val url = "$uploadUri/upload/video"
        val params = RequestParams()
        params.put("file", file)
        HttpClientUtil.removeAllHeader()
        HttpClientUtil.post(url, params, jsonHttpResponse)
    }

    fun uploadFileVoice(file: File, jsonHttpResponse: TextHttpResponseHandler) {
        val url = "$uploadUri/upload/voice"
        val params = RequestParams()
        params.put("file", file)
        HttpClientUtil.removeAllHeader()
        HttpClientUtil.post(url, params, jsonHttpResponse)
    }

    fun queryRecord(pageSize: Int, pageNum: Int, userId: String, jsonHttpResponse: BaseJsonHttpResponseHandler<ArrayList<Record>>) {
        val url = "$baseUri/point/pointPage"
        val params = RequestParams()
        params.put("pageSize", pageSize)
        params.put("pageNum", pageNum)
        params.put("userId", userId)
        HttpClientUtil.removeAllHeader()
        HttpClientUtil[url, params, jsonHttpResponse]
    }
}