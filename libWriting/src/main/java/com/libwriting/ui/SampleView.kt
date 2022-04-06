package com.libwriting.ui

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.TextPaint
import android.util.AttributeSet
import com.write.libwriting.R

/*
    这个是显示采样的视图
    根据已经有的坐标数据显示出不同的采样轨迹
 */
class SampleView(context: Context?, attrs: AttributeSet?) : PlayView(context, attrs) {
    init {
    }
    override fun resetBitmap(bm: Bitmap?) {
        bmBuf = bm?.copy(bm.config, bm.isMutable)
        postInvalidate()
    }
    /*
        把传入的整个采样坐标的路径再重复画一次
        画的时候计算两个点之间的停留时间
     */
    override fun drawPathPoint(pathList: ArrayList<ArrayList<PointEx>>) {
        super.drawPathPoint(pathList)
    }

    private fun writeWater(canvas: Canvas?) {
        var water = "采样"
        val paint = TextPaint()
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.flags = Paint.ANTI_ALIAS_FLAG
        paint.color = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) resources.getColor(R.color.light_gray, null) else resources.getColor(R.color.light_gray)
        paint.alpha = 150
        paint.textSize = (width * 0.2).toFloat()
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.createFromAsset(context.assets, context.resources.getString(R.string.kai_font))
        var fm = paint.fontMetricsInt
        var fh = fm.descent- fm.ascent
        var fw = paint.measureText(water)
        var x = width.toFloat() / 2
        var y = (height.toFloat() + fh) / 2
        canvas?.drawText(water, x, y, paint)
    }

    override fun clearAll() {
        super.clearAll()
    }

    override fun doDraw(canvas: Canvas?) {
        writeWater(canvas)
        super.doDraw(canvas)
    }
}