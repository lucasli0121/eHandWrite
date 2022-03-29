package com.libwriting.utils

import android.graphics.PointF
import com.libwriting.ui.PointEx
import kotlin.math.*

class LineUtils {
    //计算两点的延长线上一点
    fun calExtendPointInLine(pt1: PointEx, pt2: PointEx, d: Float) : Pair<Float, Float> {
        var x1 = pt1.x
        var y1 = pt1.y
        var x2 = pt2.x
        var y2 = pt2.y
        var x = x2
        var y = y2
        if (x1 == x2) {
            x = x2
            y = if(y2 > y1) y2 + d else y2 - d
        } else {
            var k = (y2 - y1) / (x2 - x1)
            var c = d.pow(2.toFloat())
            var m = sqrt(c / (1 + k.pow(2.toFloat())))
            if(x2 < x1) {
                m *= -1
            }
            x = x2 + m
            y = y2 + k*m
        }
        return x to y
    }
    /*
        计算两个点中间，距离起点为d的那个点
     */
    fun calPointInLine(pt1: PointEx, pt2: PointEx, d: Float) : Pair<Float, Float> {
        var x1 = pt1.x
        var y1 = pt1.y
        var x2 = pt2.x
        var y2 = pt2.y
        var x = x2
        var y = y2
        if (x1 == x2) {
            x = x1
            y = if(y2 > y1) y1 + d else y1 - d
        } else {
            var k = (y2 - y1) / (x2 - x1)
            var c = d.pow(2.toFloat())
            var m = sqrt(c / (1 + k.pow(2.toFloat())))
            if(x2 < x1) {
                m *= -1
            }
            x = x1 + m
            y = y1 + k*m
        }
        return x to y
    }
    /*
     计算两个点的距离
     */
    fun calDistanceWithPoints(pt1: PointEx, pt2: PointEx): Float {
        return sqrt((pt2.x - pt1.x).pow(2) + (pt2.y - pt1.y).pow(2))
    }

    /*
        计算两个圆的外公切线
        返回切线的四个点坐标
        本函数不判断两个圆是否内切
        本函数假设两个圆有外切线的情况
     */
    fun calDoubleCircleTanCoor(pt1: PointEx, pt2: PointEx): ArrayList<PointF> {
        var ptAry = ArrayList<PointF>()
        var r1: Float = if(pt1.r > 0) pt1.r else 0f
        var r2: Float = if(pt2.r > 0) pt2.r else 0f
        var x1 = pt1.x
        var y1 = pt1.y
        var x2 = pt2.x
        var y2 = pt2.y
        var d = sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
        var a: Double = if(x2 > x1) acos( abs(r1 - r2) / d).toDouble() else PI - acos(abs(r1 - r2) / d)
        var ctan:Float = 0f
        if(x1 == x2) {
            ctan = if (y1 < y2) (-(PI / 2)).toFloat() else (PI / 2).toFloat()
        } else {
            ctan = atan((y2 - y1) / (x2 - x1))
        }
        var a1: Double = 0.0
        var a2: Double = 0.0
        var pt3: PointF = PointF()
        var pt32: PointF = PointF()
        var pt4: PointF = PointF()
        var pt42: PointF = PointF()
        if (r1 < r2) {
            a1 = PI + (a + ctan)
            a2 = PI - (a - ctan)
        } else {
            a1 = (a + ctan)
            a2 = -(a - ctan)
        }
        pt3.x = (x1 + r1 * cos(a1)).toFloat()
        pt3.y = (y1 + r1 * sin(a1)).toFloat()
        pt32.x = (x1 + r1 * cos(a2)).toFloat()
        pt32.y = (y1 + r1 * sin(a2)).toFloat()
        pt4.x = (x2 + r2 * cos(a1)).toFloat()
        pt4.y = (y2 + r2 * sin(a1)).toFloat()
        pt42.x = (x2 + r2 * cos(a2)).toFloat()
        pt42.y = (y2 + r2 * sin(a2)).toFloat()
        ptAry.add(pt3)
        ptAry.add(pt32)
        ptAry.add(pt4)
        ptAry.add(pt42)
        return ptAry
    }
}