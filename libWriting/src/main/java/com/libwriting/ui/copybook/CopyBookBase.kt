package com.libwriting.ui.copybook

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.libwriting.data.DataBase
import com.libwriting.ui.CaptureTouchView
import com.libwriting.ui.DrawBaseView
import com.libwriting.ui.DrawTouchView
import com.libwriting.ui.PointEx
import com.libwriting.utils.DisplayUtils
import com.write.libwriting.R

open class CopyBookBase(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    //var binding: PaperBaseViewBinding
    protected var drawViewList = ArrayList<CaptureTouchView>()
    protected val padding = DisplayUtils.dip2px(context, 20f)
    init {
//        var inflater = LayoutInflater.from(context)
//        var v = inflater.inflate(R.layout.paper_base_view, this, true)
//        binding = PaperBaseViewBinding.bind(v)

    }

    /*
        不光是清除笔迹
        还要把所有创建的WriteView也移除掉
     */
    fun clearAll() {
        clearView()
        drawViewList.clear()
        removeAllViews()
    }

    /*
        把所有的写视图上的笔迹清除干净
     */
    fun clearView() {
        drawViewList.forEach{it.clearAll()}
    }

    /*
        是否允许手写
     */
    fun enableFinger(enable: Boolean) {
        drawViewList.forEach { it.enableFingerDraw = enable }
    }
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        val w = width - 2*padding
        val h = height - 2*padding
        if(drawViewList.size == 0) {
            initWriteViews(w, h)
        }else {
            resizeGrid(w, h)
        }
    }

    open fun initWriteViews(parentW: Int, parentH: Int) {
        clearAll()
    }

    open fun setWriteViewProperty(v: DrawTouchView) {
        v.enableFingerDraw = true
        v.thick = DrawBaseView.minThick
        v.backColor = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) resources.getColor(R.color.black, null) else resources.getColor(R.color.black)
        v.penColor = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) resources.getColor(R.color.white, null) else resources.getColor(R.color.white)
        v.gridLineChoiceColor = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) resources.getColor(R.color.red, null) else resources.getColor(R.color.red)
        v.gridLineDefColor = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) resources.getColor(R.color.white, null) else resources.getColor(R.color.white)
        v.forceUsePenColor = true
    }

    open fun resizeGrid(parentW: Int, parentH: Int) {

    }

    open fun makeBitmap(cols: Int, rows: Int, colWidth: Int): Bitmap? {
        var w = cols * colWidth
        var h = rows * colWidth
        var bmBuf = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmBuf?.setHasAlpha(true)
        var bmCanvas = Canvas(bmBuf!!)
        for (col in 0 until cols) {
            var left = col * colWidth
            for (row in 0 until rows) {
                var top = row * colWidth
                var obj = drawViewList[col*rows + row] as DrawTouchView
                var bm = obj.getBitmap()
                if (bm != null) {
                    bmCanvas.drawBitmap(bm, left.toFloat(), top.toFloat(), null)
                }
            }
        }
        return bmBuf
    }

    protected fun makePoints(): String {
        var pathMap: HashMap<Int, ArrayList<ArrayList<PointEx>>> = HashMap()
        for(i in 0 until drawViewList.size) {
            if(drawViewList[i].hasDirty) {
                pathMap[i] = drawViewList[i].getPathList()
            }
        }
        return DataBase.formatPathAryToStr(pathMap)
    }

    fun hasDrawPoints(): Boolean {
        var ret = false
        drawViewList.forEach { v ->
            if(v.hasDirty) {
                ret = true
                return@forEach
            }
        }
        return ret
    }

    protected inner class WriteChoiceListener : DrawBaseView.OnChoiceListener {
        override fun onChoice(v: DrawBaseView) {
            drawViewList.forEach {
                it.choice = it == v
            }
        }
    }
}