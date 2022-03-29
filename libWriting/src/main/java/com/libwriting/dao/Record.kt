package com.libwriting.dao

import android.content.Context
import com.handwrite.utils.PromptMessage
import com.libwriting.WriteApp
import com.libwriting.service.UploadService
import com.libwriting.utils.DaoHelper
import com.loopj.android.http.BaseJsonHttpResponseHandler
import com.loopj.android.http.TextHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import kotlin.properties.Delegates

class Record : DaoBase() {
    var traceId: String? = null
    var text: String? = null
    var pic: String? = null
    var gif: String? = null
    var video: String? = null
    var points: String? = null
    var createTime: String? = null

    public  fun fromJson(jsonObj: JSONObject): Boolean {
        if (jsonObj != null) {
            try {
                traceId = jsonObj.getString("traceId")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                text = jsonObj.getString("text")
            } catch (e: Exception) {
                e.printStackTrace()
            }


            try {
                pic = jsonObj.getString("pic")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                gif = jsonObj.getString("gif")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                video = jsonObj.getString("video")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                points = jsonObj.getString("points")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                createTime = jsonObj.getString("createTime")
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

        fun queryPageDown(userId: String, resp: RecordResponse) {
            pageNum = if(pageNum < maxPage) pageNum + 1 else maxPage
            DaoHelper.run{ queryRecord(pageSize,
                pageNum, userId, RecordHttpResponse().apply { response = resp }) }
        }
        fun queryPageUp(userId: String, resp: RecordResponse) {
            pageNum = if(pageNum > 1) pageNum - 1 else 1
            DaoHelper.run{ queryRecord(pageSize,
                pageNum, userId, RecordHttpResponse().apply { response = resp }) }
        }
    }

    interface RecordResponse {
        fun onSuccess(results: ArrayList<Record>?)
        fun onFailure(error: String?)
    }

    fun deleteRecord(resp: RecordResponse) {
        traceId?.let {
            DaoHelper.deletePoints(it, object: TextHttpResponseHandler() {
                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    responseString: String?
                ) {
                    var jsonObj = JSONObject(responseString)
                    var code = jsonObj.getInt("code")
                    if(code == 20000) {
                        resp.onSuccess(null)
                    } else {
                        resp.onFailure(jsonObj.getString("message"))
                    }
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    responseString: String?,
                    throwable: Throwable?
                ) {
                    resp.onFailure(throwable?.message)
                }

            })
        }
    }

    private class RecordHttpResponse : BaseJsonHttpResponseHandler<ArrayList<Record>>() {
        var response: RecordResponse? = null
        override fun parseResponse(p0: String?, p1: Boolean): ArrayList<Record> {
            var results = ArrayList<Record>()
            try {
                var jsonObj = JSONObject(p0)
                var code = jsonObj.getInt("code")
                if(code == 20000) {
                    var jsonData = jsonObj.getJSONObject("data")
                    maxPage = jsonData.getInt("total")
                    var jsonAry = jsonData.getJSONArray("rows")
                    for(i in 0 until jsonAry.length()) {
                        var record = Record()
                        record.fromJson(jsonAry[i] as JSONObject)
                        results.add(record)
                    }
                }
            }catch (e: Exception) {
                response?.onFailure(e.message)
            }
            return results
        }

        override fun onSuccess(
            statusCode: Int,
            headers: Array<out cz.msebera.android.httpclient.Header>?,
            rawJsonResponse: String?,
            resp: ArrayList<Record>?
        ) {
            response?.onSuccess(resp)
        }

        override fun onFailure(
            statusCode: Int,
            headers: Array<out cz.msebera.android.httpclient.Header>?,
            throwable: Throwable?,
            rawJsonData: String?,
            errorResponse: ArrayList<Record>?
        ) {
            response?.onFailure(throwable?.message)
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