package com.libwriting.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.write.libwriting.R
import java.util.*

open class DrawBaseView(context: Context?, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback  {
    private var Tag: String = "DrawBaseView"
    enum class GridType {
        NoneType,
        TianziType,
        TiaoxingType,
        MiziType,
        HuiziType,
        KouziType
    }
    enum class LineType {
        DashNoFrame,
        DashWithFrame,
        DashWithSolidFrame,
        SolidNoFrame,
        SolidWithFrame,
        SoldWithDashFrame
    }
    companion object {
        val minThick = 3.0f
        val midThick = 6.0f
        val largeThick = 9.0f
    }
    var gridType: GridType = GridType.NoneType
        set(v) {
            if(field != v) {
                field = v
                initGridBitmap()
            }
        }
    var lineType: LineType = LineType.DashWithSolidFrame
    protected var bmCanvas : Canvas? = null
    protected var bmBuf : Bitmap? = null
    private var bmGrid: Bitmap? = null
    private var surfaceDestroy: Boolean = false
    protected var surfaceCreate: Boolean = false
    var colSpace: Int = 0
    var rowSpace: Int = 0
    var thick = minThick
    var rows = 1
        set(value) {
            if(field != value) {
                field = value
                postInvalidate()
            }
        }
    var cols = 1
        set(value) {
            if(field != value) {
                field = value
                postInvalidate()
            }
        }

    var penColor: Int = resources.getColor(R.color.black, null)
    var choiceColor: Int = resources.getColor(R.color.red, null)
    var backColor: Int = resources.getColor(R.color.white, null)
        set(value) {
            if(field != value) {
                field = value
                initGridBitmap()
            }
        }
    var backBitmap: Bitmap? = null
        set(value) {
            if(field != value) {
                field = value
                postInvalidate()
            }
        }
    var gridLineDefColor: Int = resources.getColor(R.color.black, null)
        set(v) {
            if(field != v) {
                field = v
                initGridBitmap()
            }
        }
    var gridLineChoiceColor: Int = resources.getColor(R.color.red, null)
        set(v) {
            if(field != v) {
                field = v
                initGridBitmap()
            }
        }
    //增加格子类型
    var onChoiceListener: OnChoiceListener? = null
    private var thrList: LinkedList<Runnable> = LinkedList()
    open var choice = false
        set(c) {
            if(c != field) {
                field = c
                postInvalidate()
            }
        }

    init {
        //下面两句设置surfaceView背景为透明
//        setZOrderOnTop(true)
//        holder.setFormat(PixelFormat.TRANSLUCENT)
        holder.addCallback(this)
        isFocusableInTouchMode = true
        keepScreenOn = true
        Thread{
            while(true) {
                try {
                    if(thrList.size > 0) {
                        var runnable: Runnable? = null
                        synchronized(thrList) {
                            runnable = thrList.remove()
                        }
                        if(runnable != null) {
                            runnable?.run()
                        }
                    }
                    Thread.sleep(15)
                } catch (e: Exception) {

                }
            }
        }.start()
    }

    interface OnChoiceListener {
        fun onChoice(v: DrawBaseView)
    }

    /*
        初始化一个缓存bitmap，可重载
     */
    protected open fun initBitmapBuf(initCanvas: Boolean=true, hasAlpha: Boolean = true) : Bitmap? {
        var bm: Bitmap? = null
        if(width > 0 && height > 0) {
            bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            if(bm != null) {
                bm?.setHasAlpha(hasAlpha)
                if(initCanvas) {
                    bmCanvas = Canvas(bm!!)
                }
            }
        }
        return bm
    }

    open fun getBitmap() : Bitmap? {
        try {
            if (bmBuf == null) {
                Log.e(Tag, "bmBuf is null")
            }
            return bmBuf?.copy(bmBuf?.config, bmBuf!!.isMutable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    open fun clearAll() {
        synchronized(thrList) {
            thrList.clear()
        }
        if(bmBuf != null) {
            try {
                var canvas = Canvas(bmBuf!!)
                canvas.drawColor(backColor, PorterDuff.Mode.CLEAR)
            } catch (e: Exception) {

            }
            invalidate()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceCreate = true
        surfaceDestroy = false
        bmBuf = initBitmapBuf()
        if(backColor == Color.TRANSPARENT) {
            setZOrderOnTop(true)
            holder.setFormat(PixelFormat.TRANSLUCENT)
        }
        initGridBitmap()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if(bmBuf != null) {
            bmBuf?.recycle()
            bmBuf = null
        }
        bmBuf = initBitmapBuf()
        if(bmGrid != null) {
            bmGrid?.recycle()
            bmGrid = null
        }
        initGridBitmap()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surfaceDestroy = true
        surfaceCreate = false
    }

    override fun postInvalidate() {
        threadDraw()
    }

    override fun invalidate() {
        makeDraw()
    }

    private fun threadDraw() {
        if (surfaceDestroy) {
            return
        }
        synchronized(thrList) {
            thrList.push(Runnable { makeDraw() })
        }
    }

    /*
        锁定surface holder并调用doDraw，向surface canvas上画bitmap
     */
    private fun makeDraw() {
        if(holder != null && surfaceCreate && this.visibility == View.VISIBLE) {
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                doDrawBackgroud(canvas)
                doDraw(canvas)
                canvas?.let{holder.unlockCanvasAndPost(it)}
            } catch (e: Exception) {
                canvas?.let { holder.unlockCanvasAndPost(it) }
                e.printStackTrace()
            }
        }
    }
    /*
        可重载
        向canvas上画背景信息
     */
    protected open fun doDrawBackgroud(canvas: Canvas?) {
        canvas?.drawColor(backColor, PorterDuff.Mode.CLEAR)
        if(bmGrid != null) {
            canvas?.drawBitmap(bmGrid!!, 0f, 0f, null)
        }
        if(backBitmap != null) {
            canvas?.drawBitmap(backBitmap!!, 0f, 0f, null)
        }
    }
    /*
        可重载
        向canvas上画bitmap对象
     */
    protected open fun doDraw(canvas: Canvas?) {
        if(bmBuf != null) {
            canvas?.drawBitmap(bmBuf!!, 0F, 0F, null)
        }
    }


    /*
        生成不同的格子，画格子采用缓存bitmap方法
     */
    private fun initGridBitmap() {
        if(bmGrid == null) {
            if(width > 0 && height > 0) {
                bmGrid = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bmGrid?.setHasAlpha(true)
            }
        }
        if(bmGrid != null) {
            var canvas = Canvas(bmGrid!!)
            if(backColor != Color.TRANSPARENT) {
                val paint = Paint()
                paint.style = Paint.Style.FILL
                paint.color = backColor
                canvas?.drawRect(Rect(0, 0, width, height), paint)
            } else {
                canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            }
            when (gridType) {
                GridType.TianziType -> {
                    drawTianGrid(canvas)
                }
                GridType.TiaoxingType -> {
                    drawLinearGrid(canvas)
                }
                GridType.MiziType -> {
                    drawMiziGrid(canvas)
                }
                GridType.HuiziType -> {
                    drawHuiziGrid(canvas)
                }
                GridType.KouziType -> {
                    drawKouziGrid(canvas)
                }
            }
            postInvalidate()
        }
    }
    /*
        在画布上画田子格，外框为黑色，里面线为灰色
     */
    protected open fun drawTianGrid(canvas: Canvas?) {
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.flags = Paint.ANTI_ALIAS_FLAG
        if(choice) {
            paint.color = gridLineChoiceColor
        } else {
            paint.color = gridLineDefColor
        }
        var oldStroke = paint.strokeWidth
        paint.strokeWidth = 2f
        canvas?.drawRect(Rect(0, 0, width, height), paint)
        paint.strokeWidth = oldStroke
        var colW = width / cols
        var rowH = height / rows
        var left = 0f
        for(i in 0 until cols) {
            var top = 0f
            for(j in 0 until rows) {
                paint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 1f)
                paint.alpha = 180
                canvas?.drawLine(
                    left + colW / 2,
                    top,
                    left + colW / 2,
                    top + rowH,
                    paint
                )
                canvas?.drawLine(
                    left,
                    top + rowH / 2,
                    left + colW,
                    top + rowH / 2,
                    paint
                )
                if(j < (rows - 1)) {
                    paint.pathEffect = null
                    paint.alpha = 255
                    canvas?.drawLine(left, top + rowH, left + colW, top + rowH, paint)
                }
                top += rowH
            }
            if(i < (cols - 1)) {
                paint.pathEffect = null
                paint.alpha = 255
                canvas?.drawLine(left + colW, 0f, left + colW, height.toFloat(), paint)
            }
            left += colW
        }
    }
    /*
        画条形格
        条形格分竖和横
        竖只画两个边线
        横只画两个底线
        目前只是竖格子
     */
    protected open fun drawLinearGrid(canvas: Canvas?) {
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.flags = Paint.ANTI_ALIAS_FLAG
        if(choice) {
            paint.color = gridLineChoiceColor
        } else {
            paint.color = gridLineDefColor
        }
        var oldStroke = paint.strokeWidth
        paint.strokeWidth = 2f
        canvas?.drawRect(Rect(0, 0, width, height), paint)
        paint.strokeWidth = oldStroke
        var colW = width / cols
        var rowH = height / rows
        var left = 0f
        var isHTiao: Boolean = cols == 1
        for(i in 0 until cols) {
            var top = 0f
            for(j in 0 until rows) {
                paint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 1f)
                paint.alpha = 180
                if(isHTiao) {
                    canvas?.drawLine(
                        left,
                        top + rowH / 2,
                        left + colW,
                        top + rowH / 2,
                        paint
                    )
                } else {
                    canvas?.drawLine(
                        left + colW / 2,
                        top,
                        left + colW / 2,
                        top + rowH,
                        paint
                    )
                }

                if(j < (rows - 1) && isHTiao) {
                    paint.pathEffect = null
                    paint.alpha = 255
                    canvas?.drawLine(
                        left,
                        top + rowH,
                        left + colW,
                        top + rowH,
                        paint
                    )
                }
                top += rowH
            }
            if(i < (cols - 1) && !isHTiao) {
                paint.pathEffect = null
                paint.alpha = 255
                canvas?.drawLine(left + colW, 0f, left + colW, height.toFloat(), paint)
            }
            left += colW
        }
    }

    /*
        在画布上画米子格
     */
    protected open fun drawMiziGrid(canvas: Canvas?) {
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.flags = Paint.ANTI_ALIAS_FLAG
        if(choice) {
            paint.color = gridLineChoiceColor
        } else {
            paint.color = gridLineDefColor
        }
        var oldStroke = paint.strokeWidth
        paint.strokeWidth = 2f
        canvas?.drawRect(Rect(0, 0, width, height), paint)
        paint.strokeWidth = oldStroke
        var colW = width / cols
        var rowH = height / rows
        var left = 0f
        for(i in 0 until cols) {
            var top = 0f
            for(j in 0 until rows) {
                paint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 1f)
                paint.alpha = 180
                canvas?.drawLine(
                    left + colW / 2,
                    top,
                    left + colW / 2,
                    top + rowH,
                    paint
                )
                canvas?.drawLine(
                    left,
                    top + rowH / 2,
                    left + colW,
                    top + rowH / 2,
                    paint
                )
                canvas?.drawLine(left, top, left + colW, top + rowH, paint)
                canvas?.drawLine(left, top + rowH, left + colW, top, paint)
                if(j < (rows - 1)) {
                    paint.pathEffect = null
                    paint.alpha = 255
                    canvas?.drawLine(left, top + rowH, left + colW, top + rowH, paint)
                }
                top += rowH
            }
            if(i < (cols - 1)) {
                paint.pathEffect = null
                paint.alpha = 255
                canvas?.drawLine(left + colW, 0f, left + colW, height.toFloat(), paint)
            }
            left += colW
        }
    }
    /*
        在画布上画回子格
     */
    protected open fun drawHuiziGrid(canvas: Canvas?) {

    }

    /*
        在画布上画口子格，允许是否全部用虚线
     */
    protected open fun drawKouziGrid(canvas: Canvas?) {
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.flags = Paint.ANTI_ALIAS_FLAG
        paint.strokeWidth = 1f
        if(choice) {
            paint.color = gridLineChoiceColor
        } else {
            paint.color = gridLineDefColor
        }
        var colW = (width - cols * colSpace) / cols
        var rowH = (height - rows * rowSpace) / rows
        var t = 0f
        var l = 0f
        if(lineType == LineType.DashWithFrame || lineType == LineType.SoldWithDashFrame) {
            paint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 1f)
        }
        if(lineType != LineType.DashNoFrame && lineType != LineType.SolidNoFrame) {
            canvas?.drawRect(l, t, l + width, t + height, paint)
        }

        for(r in 0 until rows) {
            l = 0f
            for(c in 0 until cols) {
                when(lineType) {
                    LineType.DashNoFrame -> {
                        paint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 1f)
                        canvas?.drawLine(l,t,l + colW, t, paint)
                    }
                    LineType.DashWithFrame -> {
                        paint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 1f)
                        canvas?.drawLine(l,t,l + colW, t, paint)
                    }
                    LineType.DashWithSolidFrame -> {
                        paint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 1f)
                        if(r > 0) {
                            canvas?.drawLine(l,t,l + colW, t, paint)
                        }
                    }
                    else -> {
                        paint.pathEffect = null
                        canvas?.drawLine(l,t,l + colW, t, paint)
                    }
                }
                if(c == 0 && (lineType == LineType.DashNoFrame || lineType == LineType.SolidNoFrame)) {
                    canvas?.drawLine(l, t, l, t + rowH, paint)
                }
                if(c < (cols - 1) || (lineType == LineType.DashNoFrame || lineType == LineType.SolidNoFrame)) {
                    canvas?.drawLine(l + colW, t, l + colW, t + rowH, paint)
                }
                if(rowSpace > 0) {
                    canvas?.drawLine(l, t + rowH, l + colW, t + rowH, paint)
                }
                l += colW
                if(colSpace > 0) {
                    canvas?.drawLine(l + colSpace, t, l + colSpace, t + rowH, paint)
                    l += colSpace
                }
            }
            t += (rowH + rowSpace)
        }
    }
}