package com.libwriting.ui.copybook

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import com.libwriting.ui.CaptureTouchView
import com.libwriting.ui.DrawBaseView
import com.libwriting.ui.DrawTouchView
import com.libwriting.utils.DisplayUtils

/*
    根据每列的个数画出竖条形格
    条形格为长方形
 */
class VTiaoCopyBook(context: Context, attrs: AttributeSet?) : CopyBookBase(context, attrs) {
    private val Tag : String = "TiaoPaperView"
    private val colWidth: Int = DisplayUtils.dip2px(context, 80f)
    private var cols = 0
    init {
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    /*
        开始画 条形格
     */
    override fun initWriteViews(parentW: Int, parentH: Int) {
        var w = colWidth
        cols = parentW / w
        var left: Int = padding + (parentW - cols * w) / 2
        var top: Int = padding
        for (col in 0 until cols) {
            var bottom = parentH
            var drawView = CaptureTouchView(context, null)
            setWriteViewProperty(drawView)
            var writeParams = drawView.layoutParams
            if(writeParams == null) {
                writeParams = LayoutParams(w, parentH)
            } else {
                writeParams.width = w
                writeParams.height = parentH
            }
            drawView.layoutParams = writeParams
            drawView.enableFingerDraw = true
            drawView.layout(left, top, left + w, bottom)
            drawView.onChoiceListener = WriteChoiceListener()
            addView(drawView)
            drawViewList.add(drawView)
            left += w
        }
    }

    override fun setWriteViewProperty(v: DrawTouchView) {
        super.setWriteViewProperty(v)
        v.gridType = DrawBaseView.GridType.TiaoxingType
    }

    override fun resizeGrid(parentW: Int, parentH: Int) {
        removeAllViews()
        var w = colWidth
        cols = parentW / w
        var left: Int = padding + (parentW - cols * w) / 2
        var top: Int = padding
        for (col in 0 until cols) {
            var bottom = parentH
            var obj = drawViewList[col]
            obj.layout(left, top, left + w, bottom)
            addView(obj)
            left += w
        }
    }

    fun makeBitmap(): Bitmap? {
        return super.makeBitmap(cols, 0, colWidth)
    }
}