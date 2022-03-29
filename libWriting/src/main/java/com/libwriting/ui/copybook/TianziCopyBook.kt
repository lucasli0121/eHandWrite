package com.libwriting.ui.copybook

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import com.libwriting.ui.CaptureTouchView
import com.libwriting.ui.DrawBaseView
import com.libwriting.ui.DrawTouchView
import com.libwriting.utils.DisplayUtils

/*
    根据每列的个数画出米子格
    米子格为正方形
    米字格是由类WriteView画的
 */
class TianziCopyBook(context: Context, attrs: AttributeSet?) : CopyBookBase(context, attrs) {
    private val Tag : String = "TianZiPaperView"
    private val colWidth: Int = DisplayUtils.dip2px(context, 80f)
    private var rows = 0
    private var cols = 0
    init {
    }

    /*
        开始画米子格
     */
    override fun initWriteViews(parentW: Int, parentH: Int) {
        clearAll()
        var w = colWidth
        cols = parentW / w
        rows = parentH / w
        var left: Int = padding + (parentW - cols * w) / 2
        for (col in 0 until cols) {
            var top: Int = padding + (parentH - rows * w) / 2
            for(row in 0 until rows) {
                var drawView = CaptureTouchView(context, null)
                setWriteViewProperty(drawView)
                var writeParams = drawView.layoutParams
                if(writeParams == null) {
                    writeParams = LayoutParams(w, w)
                } else {
                    writeParams.width = w
                    writeParams.height = w
                }
                drawView.layoutParams = writeParams
                drawView.layout(left, top, left+w, top + w)
                drawView.onChoiceListener = WriteChoiceListener()
                addView(drawView)
                drawViewList.add(drawView)
                top += w
            }
            left += w
        }
    }

    override fun setWriteViewProperty(v: DrawTouchView) {
        super.setWriteViewProperty(v)
        v.gridType = DrawBaseView.GridType.TianziType
    }
    override fun resizeGrid(parentW: Int, parentH: Int) {
        removeAllViews()
        var w = colWidth
        cols = parentW / w
        rows = parentH / w
        var left: Int = padding + (parentW - cols * w) / 2
        for (col in 0 until cols) {
            var top: Int = padding + (parentH - rows * w) / 2
            for (row in 0 until rows) {
                var obj = drawViewList[col*rows + row]
                obj.layout(left, top, left + w, top + w)
                addView(obj)
                top += w
            }
            left += w
        }
    }

    fun makeBitmap(): Bitmap? {
        return super.makeBitmap(cols, rows, colWidth)
    }
}