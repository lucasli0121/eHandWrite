package com.libwriting.ui.paper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import com.libwriting.ui.CaptureTouchView
import com.libwriting.ui.DrawBaseView
import com.libwriting.ui.DrawTouchView
import com.libwriting.utils.DisplayUtils

/*
    根据每列的个数画出米子格
    米子格为正方形
    米字格是由类WriteView画的
 */
class TianZiPaperView(context: Context, attrs: AttributeSet?) : PaperBaseView(context, attrs) {
    private val Tag : String = "TianZiPaperView"


    init {
    }

    override fun setDefaultProperty() {
        super.setDefaultProperty()
        captureView.gridType = DrawBaseView.GridType.NoneType
        playView.gridType = DrawBaseView.GridType.TianziType
        playView.backColor = Color.BLACK
//        playView.visibility = View.GONE
//        captureView.visibility = View.VISIBLE
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        captureView.cols = width / gridWidth
        captureView.rows = height / gridHeight
        playView.cols = width / gridWidth
        playView.rows = height / gridHeight
    }
}