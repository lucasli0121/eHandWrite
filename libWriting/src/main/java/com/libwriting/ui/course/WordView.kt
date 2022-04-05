package com.libwriting.ui.course

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.SurfaceHolder
import com.libwriting.data.DataBase
import com.libwriting.ui.DrawView
import com.libwriting.ui.PointEx
import com.write.libwriting.R

/*
    实现系统字库，标准字的视图类
    用于显示字库中的汉字
 */
class WordView(context: Context?, attrs: AttributeSet?) : DrawView(context, attrs) {
    var word: String? = null
        set(value: String?) {
            if(value != field) {
                field = value
                drawWord()
            }
        }
    var wordId: String? = null
    var useBlackClr: Boolean = false
        set(value: Boolean) {
            if(value != field) {
                field = value
                drawWord()
            }
        }
    private var ttf: String? = context?.resources?.getString(R.string.kai_font)
    private var typeFace: Typeface = Typeface.createFromAsset(context?.assets, ttf)
    private var pathList: ArrayList<ArrayList<PointEx>>? = null
    override var choice = false
        set(c) {
            if(c != field) {
                field = c
                drawWord()
                drawPathPoint()
            }
        }

    override fun surfaceCreated(holder: SurfaceHolder) {
        super.surfaceCreated(holder)
        drawWord()
        drawPathPoint()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        super.surfaceChanged(holder, format, width, height)
        drawWord()
        drawPathPoint()
    }

    fun changeTtf(t: DataBase.TtfType) {
        ttf = when(t) {
            DataBase.TtfType.Kai -> context?.resources?.getString(R.string.kai_font)
            DataBase.TtfType.Xing -> context?.resources?.getString(R.string.xing_font)
        }
        typeFace = Typeface.createFromAsset(context?.assets, ttf)
    }

    private fun drawWord() {
        if(word != null && surfaceCreate) {
            bmCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            val paint = TextPaint()
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.flags = Paint.ANTI_ALIAS_FLAG
            if(useBlackClr) {
                paint.color = Color.BLACK
            } else if (choice) {
                paint.color = gridLineChoiceColor
            } else {
                paint.color = penColor
            }
            paint.textSize = (width * 0.8).toFloat()
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.createFromAsset(context.assets, ttf)
            var fm = paint.fontMetricsInt

            var fh = fm.descent - fm.ascent
            var fw = paint.measureText(word!!)
            var x = width.toFloat() / 2
            var y = height.toFloat() - fm.descent - fm.leading //(height.toFloat() + fh) / 2
            bmCanvas?.drawText(word!!, x, y, paint)
            postInvalidate()
        }
    }

    /*
        外部调用，用于根据轨迹字符串画字
     */
    fun drawPathFromStr(pointStr: String) {
        pathList = DataBase.parsePathFromStr(pointStr)
        drawPathPoint()
    }
    /*
        把传入的整个采样坐标的路径再重复画一次
        画的时候计算两个点之间的停留时间
     */
    private fun drawPathPoint() {
        if(pathList == null || width == 0) {
            return
        }
        clearAll()
        for (i in pathList!!.indices) {
            var ptList = pathList!![i]
            for (n in ptList.indices) {
                var pt1 = ptList[n]  //
                pt1.w = width
                pt1.h = height
                if(n < ptList.size - 1) {
                    drawPointEx(pt1, n == 0)
                } else {
                    drawEndPointEx(pt1)
                }
            }
        }
//        var trace: TrackPoint = TrackPoint()
//        trace.wordId = "1"
//        trace.type = TrackPoint.Type.PCEmPen.name
//        trace.ttf = TrackPoint.TtfType.Kai.name
//        trace.points = ""
//        bmBuf?.let { trace.putPic(context, it) }
    }

}