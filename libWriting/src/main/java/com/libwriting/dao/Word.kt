package com.libwriting.dao

import android.content.Context
import com.handwrite.utils.PromptMessage
import com.libwriting.WriteApp
import com.libwriting.service.UploadService
import com.libwriting.utils.DaoHelper
import com.loopj.android.http.BaseJsonHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import kotlin.properties.Delegates

class Word : DaoBase() {
    lateinit var id: String
   // lateinit var GradeID: String
    lateinit var text: String
  //  var desc: String = ""
    var userId: String? = null
    var userName: String? = null

    fun hasWrite(context: Context) : Boolean {
        var ret: Boolean = false
        try {
            var app: WriteApp = context.applicationContext as WriteApp
            if (userId != null
                && userName != null
                && app.teacher.id != null
                && userId == app.teacher.id
            ) {
                ret = true
            }
        }catch (e: Exception) {
            ret = false
        }
        return ret
    }

    fun fromJson(jsonObj: JSONObject): Boolean {
        if (jsonObj != null) {
            try {
                id = jsonObj.getString("id")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
              //  GradeID = jsonObj.getString("GradeID")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                text = jsonObj.getString("text")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                userId = jsonObj.getString("userId")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                userName = jsonObj.getString("userName")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    companion object {
        const val pageSize: Int = 10
        var pageNum: Int = 1
        var maxPage: Int = 10
        fun queryPageDown(userId: String, id: String, resp: WordResponse) {
            pageNum = if(pageNum < maxPage) pageNum + 1 else maxPage
            DaoHelper.run{ queryWord(userId, id, pageSize, pageNum, WordHttpResponse().apply { response = resp }) }
        }
        fun queryPageUp(userId: String, id: String, resp: WordResponse) {
            pageNum = if(pageNum > 1) pageNum - 1 else 1
            DaoHelper.run{ queryWord(userId, id, pageSize, pageNum, WordHttpResponse().apply { response = resp }) }
        }
    }

    interface WordResponse {
        fun onSuccess(results: ArrayList<Word>?)
        fun onFailure(error: String?)
    }
    /*
        内部类，这个类用来从后台获取系统字库数据
        如果获取数据成功，则调用initWord()来处理UI显示
     */
    private class WordHttpResponse : BaseJsonHttpResponseHandler<ArrayList<Word>>() {
        var response: WordResponse? = null
        override fun onSuccess(status: Int, p1: Array<out Header>?, p2: String?, results: ArrayList<Word>?) {
            if(status == 200 && results != null) {
                System.out.println("sttus---------"+status)
                response?.onSuccess(results)
            }
        }
        override fun onFailure(p0: Int, p1: Array<out Header>?, p2: Throwable?, p3: String?, p4: ArrayList<Word>?) {
            response?.onFailure(p2?.message)
        }

        override fun parseResponse(jsonRaw: String?, p1: Boolean): ArrayList<Word> {
            System.out.println("jsonRaw----"+jsonRaw.toString())
            var results = ArrayList<Word>()
            try {
                var jsonObj = JSONObject(jsonRaw)
                if (jsonObj != null) {
                    var code = jsonObj.getInt("code")
                    if(code == 20000) {
                        var jsonObj = jsonObj.getJSONObject("data")
                        var total = jsonObj.getInt("total")
                        maxPage = total / pageSize + if(total % pageSize > 0) 1 else 0
                        var jsonAry = jsonObj.getJSONArray("rows")
                        for (i in 0 until jsonAry.length()) {
                            var w = Word()
                            w.fromJson(jsonAry.getJSONObject(i))
                            results.add(w)
                        }
                    } else {
                        var msg = jsonObj.getString("data")
                        response?.onFailure(msg)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return results
        }

    }

    override fun submit(
        context: Context?,
        uploadService: UploadService,
        resp: OnSubmitResponse,
        taskId: Int?
    ) {
    }

    override fun submit(context: Context?, resp: OnSubmitResponse, taskId: Int?) {
    }
}