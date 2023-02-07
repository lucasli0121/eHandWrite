package com.libwriting.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.libwriting.utils.LineUtils
import com.libwriting.R
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

class PointEx {
    var x: Float = 0f
        set(value) {
            field = if(w == 0) {
                value
            } else {
                value / w
            }
        }
        get() {
            var v = if(w == 0) field else field * w
            return v
        }
    var y: Float = 0f
        set(value) {
            field = if(h == 0) {
                value
            } else {
                value / h
            }
        }
        get() {
            var v = if(h == 0) field else field * h
            return v
        }
    var r: Float = 0f
    var ns: Long = 0
    var w: Int = 0
    var h: Int = 0
    init {
        ns = SystemClock.elapsedRealtimeNanos()
    }
    fun draw(view: View, canvas: Canvas?, p: Paint) {
        canvas ?.drawCircle(x, y, r, p)
        view.postInvalidate()
    }
    fun calcTmDiff(lastPt: PointEx?): Pair<Long, Long> {
        var diffs = ns - (lastPt?.ns ?: 0)
        var dms = diffs / 1000000
        var dns = diffs % 1000000
        return Pair(dms,dns)
    }
}

open class DrawView(context: Context?, attrs: AttributeSet?) : DrawBaseView(context, attrs)  {
    private val Tag : String = "DrawView"
    // 本类中ptLst保存需要画的点
    // 怎么清除由子类负责
    protected var ptLst = ArrayList<PointEx>()
    var forceUsePenColor: Boolean = false

    override fun clearAll() {
        ptLst.clear()
        super.clearAll()
    }

    protected open fun makePointEx(x: Float, y: Float, z: Float, fingerDraw: Boolean = false): PointEx {
        return PointEx().apply {
            this.w = width
            this.h = height
            this.x = x
            this.y = y
            this.r = if(fingerDraw) z else z * thick
        }
    }
    private fun makeDrawPaint(): Paint {
        val paint = Paint()
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.flags = Paint.ANTI_ALIAS_FLAG
        if(forceUsePenColor) {
            paint.color = penColor
        } else {
            paint.color = if (choice) choiceColor else penColor
        }
        return paint
    }
    /*
        画一个点
     */
    protected open fun drawPointEx(pt: PointEx, isFirst: Boolean = false, canvas: Canvas? = null, paint: Paint? = null) {
        synchronized(ptLst) {
            if (isFirst) {
                ptLst.clear()
            }
            // 过滤点，把不符合要求的点过滤掉
            if(filterPointNoise(pt)) {
                return@synchronized
            }
            if (ptLst.isEmpty()) {
                // 画每一笔的第一个点
                pt.draw(this, canvas ?: bmCanvas, paint ?: makeDrawPaint())
            } else {
                // 画连续的两个点
                var lastpt = ptLst[ptLst.size - 1]
                drawMorePoints(lastpt, pt, canvas, paint)
            }
            ptLst.add(pt)
        }
    }

    /*
        根据算法计算屏幕上两个点的画法，并在convas上画
     */
    private fun drawMorePoints(pt1: PointEx, pt2: PointEx, canvas: Canvas? = null, p: Paint? = null )  {
        var dis = LineUtils().calDistanceWithPoints(pt1, pt2)
        if ( dis <= 0 ) {
            pt2.draw(this, canvas ?: bmCanvas, p ?: makeDrawPaint())
        } else {
            var dr = pt2.r - pt1.r
            var sr = dr / dis
            var r = pt1.r
            for( d1 in 0 until dis.toInt()) {
                var inPt = LineUtils().calPointInLine(pt1, pt2, d1.toFloat())
                var pt = PointEx().apply {
                    this.x = inPt.first
                    this.y = inPt.second
                    this.r = r
                }
                pt.draw(this, canvas ?: bmCanvas, p ?: makeDrawPaint())
                r += sr
                if(dr < 0) {
                    if(r < pt2.r) {
                        r = pt2.r
                    }
                } else if (r > pt2.r) {
                    r = pt2.r
                }
            }
        }
    }
    /*
        画结束的点
     */
    protected open fun drawEndPointEx(pt: PointEx, canvas: Canvas? = null, paint: Paint? = null) {
        synchronized(ptLst) {
            // 从后往前循环查找一个和pt坐标不相同的点，再画结尾
            for (i in ptLst.size - 1 downTo 0) {
                if(ptLst[i].x != pt.x || ptLst[i].y != pt.y) {
                    writeEndPoints(ptLst[i], pt, canvas, paint ?: makeDrawPaint())
                    break
                }
            }
            ptLst.add(pt)
        }
    }
    /*
        根据结束算法，画两个点，能画出结束点的延长线
        优化画线方式
     */
    private fun writeEndPoints(pt1: PointEx, pt2: PointEx, canvas: Canvas? = null, p: Paint? = null) {
        drawMorePoints(pt1, pt2, canvas, p)
        var d = LineUtils().calDistanceWithPoints(pt1, pt2)
        while (d < max(pt1.r, pt2.r) || d <= 0) {
            d += 1
        }
        var extpt = LineUtils().calExtendPointInLine(pt1, pt2, d)
        var endpt = PointEx().apply {
            this.x = extpt.first
            this.y = extpt.second
            this.r = 0.0f
        }
        Log.d(Tag, "writeEndPoints, d=${d},pt1.x=${pt1.x},y=${pt1.y},pt2.x=${pt2.x},y=${pt2.y} endpt.x=${endpt.x}, endpt.y=${endpt.y}")
        drawMorePoints(pt2, endpt, canvas, p)
    }

    /*
        清洗
        1、两个坐标不同但距离过近的点清洗掉
     */
    private fun filterPointNoise(pt: PointEx): Boolean {
        var ret = false
        if(ptLst.size <= 1) {
            return ret
        }
//        var pt1 = ptLst[ptLst.size - 2]
//        var pt2 = ptLst[ptLst.size - 1]
//        if((pt1.x == pt2.x && pt.x == pt2.x)
//            || (pt1.y == pt2.y && pt.y == pt2.y)) {
//            var d = LineUtils().calDistanceWithPoints(pt1, pt2)
//            var d1 = LineUtils().calDistanceWithPoints(pt1, pt)
//            ret = d1 < d
//            if(ret) {
//                Log.d(Tag, "filterPointNoise is $ret")
//            }
//        }
        var pt1 = ptLst[ptLst.size - 1]
        // 坐标相同直接返回
        if(pt1.x == pt.x && pt1.y == pt.y) {
            return ret
        }
        var d = LineUtils().calDistanceWithPoints(pt1, pt)
        ret = d < 2
        return ret
    }
}