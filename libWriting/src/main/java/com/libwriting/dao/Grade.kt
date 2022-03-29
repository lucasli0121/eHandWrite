package com.libwriting.dao

import android.content.Context
import com.handwrite.utils.PromptMessage
import com.libwriting.service.UploadService
import com.libwriting.utils.DaoHelper
import com.loopj.android.http.BaseJsonHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import kotlin.properties.Delegates

class Grade : DaoBase() {
    lateinit var id: String
    lateinit var name: String
    var level by Delegates.notNull<Int>()

    public  fun fromJson(jsonObj: JSONObject): Boolean {
        if (jsonObj != null) {
            try {
                id = jsonObj.getString("id")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                name = jsonObj.getString("name")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                level = jsonObj.getInt("level")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    companion object {
        fun queryGrade( resp: GradeResponse) {
            DaoHelper.refreshGrade(GradeHttpResponse().apply { response = resp })
        }
    }

    interface GradeResponse {
        fun onSuccess(results: ArrayList<Grade>?)
        fun onFailure(error: String?)
    }
    /*
            向后端服务器查询年级数据处理类，被DaoHelper回调
         */
    private class GradeHttpResponse: BaseJsonHttpResponseHandler<ArrayList<Grade>>() {
        var response: GradeResponse? = null
        override fun onSuccess(status: Int, p1: Array<out Header>?, p2: String?, results: ArrayList<Grade>?) {
            if(status == 200 && results != null) {
                response?.onSuccess(results)
            }

        }

        override fun onFailure(
            p0: Int,
            p1: Array<out Header>?,
            p2: Throwable?,
            p3: String?,
            p4: ArrayList<Grade>?
        ) {
            response?.onFailure(p2?.message)
        }

        override fun parseResponse(jsonRaw: String?, p1: Boolean): ArrayList<Grade> {
            var results = ArrayList<Grade>()
            try {
                var jsonObj = JSONObject(jsonRaw)
                if (jsonObj != null) {
                    var code = jsonObj.getInt("code")
                    if(code == 20000) {

                        var jsonAry = jsonObj.getJSONArray("data")
                        for (i in 0 until jsonAry.length()) {
                            var grade = Grade()
                            grade.fromJson(jsonAry.getJSONObject(i))
                            results.add(grade)
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