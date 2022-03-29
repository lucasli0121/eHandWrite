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
    根据每列的个数画出竖条形格
    条形格为长方形
 */
class TiaoPaperView(context: Context, attrs: AttributeSet?) : PaperBaseView(context, attrs) {
    private val Tag : String = "TiaoPaperView"
    private val colWidth: Int = DisplayUtils.dip2px(context, 80f)
    var isVTiaoxing: Boolean = true
    init {
    }

    override fun setDefaultProperty() {
        super.setDefaultProperty()
        captureView.gridType = DrawBaseView.GridType.NoneType
        playView.gridType = DrawBaseView.GridType.TiaoxingType
        playView.backColor = Color.BLACK
//        playView.visibility = View.GONE
//        captureView.visibility = View.VISIBLE
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if(isVTiaoxing) {
            setVGridProp()
        } else {
            setHGridProp()
        }
    }

    fun setVGridProp() {
        var colW = gridWidth
        var rowH = height
        captureView.cols = width / colW
        captureView.rows = height / rowH
        playView.cols = width / colW
        playView.rows = height / rowH
    }
    fun setHGridProp() {
        var colW = width
        var rowH = gridHeight
        captureView.cols = width / colW
        captureView.rows = height / rowH
        playView.cols = width / colW
        playView.rows = height / rowH
    }
}