package com.libwriting.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import com.libwriting.data.DataBase
import com.libwriting.utils.VoiceMgr
import java.io.File

/*
    这个是显示采样的视图
    根据已经有的坐标数据显示出不同的采样轨迹
 */
open class PlayView(context: Context?, attrs: AttributeSet?) : DrawView(context, attrs) {
    private var Tag: String = "PlayView"
    var drawDot: Boolean = false
    private var voiceMgr: VoiceMgr? = null
    private var thread: Thread? = null
    var playNum: Int = Int.MAX_VALUE
    var stopPlay: Boolean = false
    init {
    }
    open fun resetBitmap(bm: Bitmap?) {
        if(bm == null) {
            return
        }
        try {
            var canvas = Canvas(bmBuf!!)
            canvas.drawBitmap(bm!!, 0f, 0f, null)
            postInvalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        super.surfaceChanged(holder, format, width, height)
        clearAll()
    }

    fun stopPlay() {
        stopPlay = true
        if(thread != null) {
            try {
                thread?.interrupt()
                thread?.join()
            } catch (e: Exception) {}
            thread = null
        }
    }

    fun playVoice(voiceFile: String) {
        try {
            if(voiceMgr != null) {
                voiceMgr?.stopPlay()
                voiceMgr = null
            }
            voiceMgr = VoiceMgr(context, File(voiceFile))
            voiceMgr?.startPlay()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun  drawPathFromStr(pointStr: String) {
        synchronized(this) {
            var pathList = DataBase.parsePathFromStr(pointStr)
            drawPathPoint(pathList)
        }
    }
    /*
        把传入的整个采样坐标的路径再重复画一次
        画的时候计算两个点之间的停留时间
     */
    open fun drawPathPoint(pathList: ArrayList<ArrayList<PointEx>>) {
        if(thread != null) {
            try {
                thread?.interrupt()
                thread?.join()
            } catch (e: Exception) {}
        }
        stopPlay = false
        thread = Thread {
            try {
                for (i in 0 until playNum) {
                    clearAll()
                    for (i in pathList!!.indices) {
                        if(stopPlay) {
                            return@Thread
                        }
                        var ptList = pathList[i]
                        var lastPathPt: PointEx? = null
                        for (n in ptList.indices) {
                            var pt1 = ptList[n]  //
                            pt1.w = width
                            pt1.h = height
                            if (drawDot) { // 只画点
                                drawOnePoint(pt1)
                            } else { // 画线
                                if (n < ptList.size - 1) {
                                    drawPointEx(pt1, n == 0)
                                } else {
                                    drawEndPointEx(pt1)
                                }
                            }
                            if (n < ptList.size - 1) {
                                var pt2 = ptList[n + 1]
                                pt2.w = width
                                pt2.h = height
                                var (ms, ns) = pt2.calcTmDiff(pt1)
                                Thread.sleep(ms, ns.toInt())
                            }
                            if (n == ptList.size - 1) {
                                lastPathPt = ptList[n]
                            }
                        }
                        if (i < pathList.size - 1) {
                            // 计算下一笔的起始点与上一笔的最有一个点的时间差,并延时
                            var nextPt = pathList[i + 1][0]
                            var (ms, ns) = nextPt.calcTmDiff(lastPathPt)
                            Thread.sleep(ms, ns.toInt())
                        }
                    }
                    Thread.sleep(2000)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread?.start()
    }

    private fun drawOnePoint(p: PointEx) {
        val paint = Paint().apply {
            this.style = Paint.Style.FILL_AND_STROKE
            this.flags = Paint.ANTI_ALIAS_FLAG
            this.color = penColor
        }
        bmCanvas ?.drawCircle(p.x, p.y, p.r, paint)
        postInvalidate()
    }

    override fun clearAll() {
        super.clearAll()
    }

    override fun doDraw(canvas: Canvas?) {
        super.doDraw(canvas)
    }

    fun handleDrawObserver(t: DrawTouchView.DrawTouchObj) {
        t.pt?.w = width
        t.pt?.h = height
        Log.d(Tag, "${t.pt?.x}:${t.pt?.y}")
        if(drawDot) {
            t.pt?.let { drawOnePoint(it) }
        } else {
            when (t.status) {
                DrawTouchView.TouchStatus.DOWN -> {
                    t.pt?.let { drawPointEx(it, true) }
                }
                DrawTouchView.TouchStatus.DOWN_MOVE -> {
                    t.pt?.let { drawPointEx(it, false) }
                }
                DrawTouchView.TouchStatus.UP -> {
                    t.pt?.let { drawEndPointEx(it) }
                }
            }
        }
    }
}