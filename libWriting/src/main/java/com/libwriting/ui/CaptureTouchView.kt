package com.libwriting.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import com.libwriting.data.DataBase

/*
    用于采集的写字视图类，从DrawTouchView继承
    父类通过touch event获取手写事件
    CaptureTouchView采集相关特征
 */
open class CaptureTouchView(context: Context?, attrs: AttributeSet?) : DrawTouchView(context, attrs) {
    private val Tag : String = "CaptureTouchView"
    // pathList 保存每一个笔画的坐标，按照笔画的顺序保存坐标点
    private var pathList = ArrayList<ArrayList<PointEx>>()
    // 书写的每一笔保存成一个bitmap对象，所有bitmap对象保存成一个bmList
    private var bmList = ArrayList<Bitmap>()
    // 保留当前正在使用的画布bitmap对象,curBm是bmList列表最后一个元素
    // curBm每一次变动都会在framelist中增加一个拷贝
    private var curBm : Bitmap? = null
    // bmIndex是frameList元素在bmlist中对应的笔画索引
    // bmIndex是 bmlist最后一个元素的位置
    private var bmIndex: Int = 0
    // 保留上一次画点的时间 ms
    private var lastDrawTm: Long = 0
    var hasDirty: Boolean = false
        get() {
            field = pathList.size > 0
            return field
        }

    override fun getBitmap() : Bitmap? {
        return drawBitmap2(false)
        //return super.getBitmap()
    }
    fun getBitmapWithBackcolor(): Bitmap? {
        return drawBitmap2(true)
    }

    fun getPathList(): ArrayList<ArrayList<PointEx>> {
        return pathList
    }

    /*
        把已经录入的笔画路径转换成设定格式的字符串
        格式固定
        转换的字符串用来保存到后台数据库
     */
    fun formatPathToStr(): String {
        return DataBase.formatPathToStr(pathList)
    }
    /*
        后退撤回一步
     */
    fun redo() {
        // 回退一个笔画bitmap
        if(bmList.size > 0) {
            var index = bmList.size - 1
            synchronized(bmList) {
                var bm = bmList[index]
                bm.recycle()
                bmList.removeAt(index)
            }
        }
        // 回退一个笔画的坐标点
        if(pathList.size > 0) {
            synchronized(pathList) {
                pathList.removeAt(pathList.size - 1)
            }
        }
        postInvalidate()
    }

    /*
        清除所有的痕迹
     */
    override fun clearAll() {
        synchronized(pathList) {
            pathList.clear()
        }
        synchronized(bmList) {
            bmList.forEach { it.recycle() }
            bmList.clear()
        }
        bmIndex = 0
        super.clearAll()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        super.surfaceCreated(holder)
    }
    /*
      把bmlist中所有的bitmap，合并成一个bitmap，合并后的bitmap是bmBuf
     */
    private fun drawBitmap(){
//        if(bmBuf != null) {
//            bmBuf?.recycle()
//            bmBuf = null
//        }
//        bmBuf = initBitmapBuf(false)
        if(bmBuf == null) {
            bmBuf = initBitmapBuf(false)
        }
        var canvas = Canvas(bmBuf!!)
        synchronized(bmList) {
            if (bmIndex == (bmList.size - 1)) {
                canvas?.drawBitmap(bmList[bmIndex], 0f, 0f, null)
            } else {
                canvas?.drawColor(backColor, PorterDuff.Mode.CLEAR)
                if(bmList.size > 0) {
                    bmList.forEach { canvas?.drawBitmap(it, 0f, 0f, null) }
                    bmIndex = bmList.size - 1
                }
            }
        }
    }
    private fun drawBitmap2(withBackColor: Boolean): Bitmap?{
        if(bmList.size == 0) {
            return null
        }
        var bm = initBitmapBuf(false)
        var canvas = Canvas(bm!!)
        if(withBackColor) {
            doDrawBackgroud(canvas)
        }
        synchronized(bmList) {
            bmList.forEach { canvas?.drawBitmap(it, 0f, 0f, null) }
        }
        return bm
    }

    override fun readyDraw() {
        super.readyDraw()
        curBm = initBitmapBuf()
        synchronized(bmList) {
            bmList.add(curBm!!)
            bmIndex = bmList.size - 1
        }
    }

    override fun inDrawing(pt: PointEx) {
        super.inDrawing(pt)
    }

    /*
        结束一个笔画
     */
    override fun endDraw(pt: PointEx, pts: ArrayList<PointEx>) {
        synchronized(pathList) {
            pathList.add(pts.clone() as ArrayList<PointEx>)
        }
    }

    /*
        重载函数
        被父类DrawBaseView调用
        参数 canvas不是bmCanvas而是父类的surfaceView的canvas
     */
    override fun doDraw(canvas: Canvas?) {
        drawBitmap()
        //调用父类doDraw
        super.doDraw(canvas)
    }
}