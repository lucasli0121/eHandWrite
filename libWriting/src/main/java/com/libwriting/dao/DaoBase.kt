package com.libwriting.dao

import android.content.Context
import com.libwriting.service.UploadService
import com.libwriting.ui.PointEx
import com.loopj.android.http.TextHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class DaoBase: Any() {
    enum class TtfType(s: String) {
        Kai("kai"),
        Xing("xing")
    }
    enum class GridType(v: Int) {
        MiziType(1),
        TianziType(2),
        KouziType(3),
        ShutiaoType(4),
        HengtiaoType(5)
    }
    companion object {
        val poetryType: Int = 1
        fun formatPathAryToStr(pathMap: HashMap<Int, ArrayList<ArrayList<PointEx>>>): String {
            var fmtStr = ""
            pathMap.forEach { (t, u) ->
                var str = formatPathToStr(u)
                fmtStr += "{${t}:${str}}"
            }
            return fmtStr
        }

        fun formatPathToStr(pathList: ArrayList<ArrayList<PointEx>>): String {
            var pathStr = ""
            try {
                for (i in pathList!!.indices) {
                    var ptList = pathList[i]
                    var ptStr = ""
                    for (n in ptList.indices) {
                        var pt1 = ptList[n]  //得到轨迹一个点的坐标
                        pt1.w = 0
                        pt1.h = 0
                        if (n == 0) {
                            ptStr = "(${pt1.x},${pt1.y},${pt1.r},${pt1.ns})"
                        } else {
                            ptStr += ",(${pt1.x},${pt1.y},${pt1.r},${pt1.ns})"
                        }
                    }
                    if (i == 0) {
                        pathStr = "[${ptStr}]"
                    } else {
                        pathStr += ",[${ptStr}]"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return pathStr
        }
        fun parsePathAryFromStr(fmtStr: String): HashMap<Int, ArrayList<ArrayList<PointEx>>> {
            var pathMap: HashMap<Int, ArrayList<ArrayList<PointEx>>> = HashMap()
            val p = Pattern.compile("(\\d+):(\\{(\\[(\\(\\d+\\.\\d*,\\d+\\.\\d*,\\d+\\.\\d*,\\d+\\),?)+\\],?)+\\})")
            val m = p.matcher(fmtStr)
            var start = 0
            while (m.find(start)) {
                val kStr: String = m.group(1)
                val vStr: String = m.group(2)
                var pathList = parsePathFromStr(vStr)
                pathMap[kStr.toInt()] = pathList
                start = m.end()
            }
            return pathMap
        }

        fun parsePathFromStr(pathStr: String): ArrayList<ArrayList<PointEx>> {
            var pathAry: ArrayList<ArrayList<PointEx>> = ArrayList()
            val p = Pattern.compile("(\\[(\\(\\d+\\.\\d*,\\d+\\.\\d*,\\d+\\.\\d*,\\d+\\),?)+\\])")
            val m = p.matcher(pathStr)
            var start = 0
            while (m.find(start)) {
                val pstr: String = m.group(1)
                val p2: Pattern =
                    Pattern.compile("(\\(\\d+\\.\\d*,\\d+\\.\\d*,\\d+\\.\\d*,\\d+\\))")
                val m2: Matcher = p2.matcher(pstr)
                var start2 = 0
                var ptList = ArrayList<PointEx>()
                while (m2.find(start2)) {
                    val ptStr: String = m2.group(1)
                    val p3: Pattern =
                        Pattern.compile("(\\d+\\.\\d*),(\\d+\\.\\d*),(\\d+\\.\\d*),(\\d+)")
                    val m3: Matcher = p3.matcher(ptStr)
                    if (m3.find()) {
                        val x = m3.group(1).toFloat()
                        val y = m3.group(2).toFloat()
                        val z = m3.group(3).toFloat()
                        val t = m3.group(4).toLong()
                        ptList.add(PointEx().apply {
                            this.x = x
                            this.y = y
                            this.r = z
                            this.ns = t })
                    }
                    start2 = m2.end()
                }
                pathAry.add(ptList)
                start = m.end()
            }
            return pathAry
        }
    }
    abstract fun submit(context: Context?, uploadService: UploadService, resp: OnSubmitResponse, taskId: Int?)
    abstract fun submit(context: Context?, resp: OnSubmitResponse, taskId:Int?)

    interface OnSubmitResponse {
        fun onSuccess(code: Int, msg: String?, taskId: Int?)
        fun onFailure(error: String?, taskId: Int?)
    }

    // 内部类，用于提交采集数据后的回应结果
    // 根据后台回应结果，进行UI的不同显示
    protected inner class InsertResponse: TextHttpResponseHandler() {
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