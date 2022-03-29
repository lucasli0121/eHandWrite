package com.libwriting.ui.paper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.libwriting.dao.DaoBase
import com.libwriting.ui.*
import com.libwriting.utils.DisplayUtils
import com.write.libwriting.R

open class PaperBaseView(context: Context, attrs: AttributeSet?) : CapturePlayView(context, attrs) {
    //var binding: PaperBaseViewBinding
    var gridWidth: Int = DisplayUtils.dip2px(context, 80f)
    var gridHeight: Int = DisplayUtils.dip2px(context, 80f)
    init {
//        var inflater = LayoutInflater.from(context)
//        var v = inflater.inflate(R.layout.paper_base_view, this, true)
//        binding = PaperBaseViewBinding.bind(v)
        setDefaultProperty()
    }

    /*
        是否允许手写
     */
    fun enableFinger(enable: Boolean) {
        this.captureView.enableFingerDraw = enable
    }

    protected open fun setDefaultProperty() {
        captureView.enableFingerDraw = false
        captureView.thick = DrawBaseView.minThick
        captureView.penColor = Color.WHITE
        captureView.gridLineChoiceColor = Color.RED
        captureView.gridLineDefColor = Color.WHITE
        captureView.forceUsePenColor = true

        playView.thick = DrawBaseView.minThick
        playView.penColor = Color.WHITE
        playView.gridLineChoiceColor = Color.RED
        playView.gridLineDefColor = Color.WHITE
        playView.forceUsePenColor = true

//        playView.visibility = View.GONE
//        captureView.visibility = View.VISIBLE
    }

    open fun playPoints(points: String) {
//        playView.visibility = View.VISIBLE
//        captureView.visibility = View.GONE
        playView.drawPathFromStr(points)
    }

    open fun enableWrite() {
        playView.visibility = View.GONE
        captureView.visibility = View.VISIBLE
    }
}