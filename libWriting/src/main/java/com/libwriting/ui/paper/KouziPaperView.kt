package com.libwriting.ui.paper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import com.libwriting.ui.CaptureTouchView
import com.libwriting.ui.DrawBaseView
import com.libwriting.ui.DrawTouchView
import com.libwriting.utils.DisplayUtils

/*
    根据每列的个数画出口子格子
 */
class KouziPaperView(context: Context, attrs: AttributeSet?) : PaperBaseView(context, attrs) {
    private val Tag : String = "KouziPaperView"
    private val colWidth: Int = DisplayUtils.dip2px(context, 80f)
    var withSolidFrame: Boolean = false
    init {
    }

    override fun setDefaultProperty() {
        super.setDefaultProperty()
        captureView.gridType = DrawBaseView.GridType.NoneType
        playView.gridType = DrawBaseView.GridType.KouziType
        playView.lineType = DrawBaseView.LineType.SolidNoFrame
        playView.backColor = Color.WHITE
        playView.gridLineDefColor = Color.RED
//        playView.visibility = View.GONE
//        captureView.visibility = View.VISIBLE
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        setGridProp()
    }

    fun setGridProp() {
        var colW = gridWidth
        var rowH = gridWidth
        val colSpace = 0
        val rowSpace = 20
        captureView.rowSpace = rowSpace
        captureView.cols = width / (colW+colSpace)
        captureView.rows = height / (rowH+rowSpace)
        playView.rowSpace = rowSpace
        playView.cols = width / (colW+colSpace)
        playView.rows = height / (rowH+rowSpace)
    }
}