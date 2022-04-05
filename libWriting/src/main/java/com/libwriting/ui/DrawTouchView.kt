package com.libwriting.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.TOOL_TYPE_STYLUS
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.lifecycle.Observer
import java.util.*
import kotlin.collections.ArrayList

/*
    实现自定义的HorizontalScrollView类，用来防止onTouchEvent事件被抢占
 */
class HScrollView(context: Context?, attrs: AttributeSet?) : HorizontalScrollView(context, attrs) {
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        var type = ev?.getToolType(0)
        if(type == TOOL_TYPE_STYLUS ) {
            return false
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        var type = ev?.getToolType(0)
        if(type == TOOL_TYPE_STYLUS) {
            return false
        }
        return super.onTouchEvent(ev)
    }
}
class VScrollView(context: Context?, attrs: AttributeSet?) : ScrollView(context, attrs) {
    private val Tag : String = "VScrollView"

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if(ev?.action == MotionEvent.ACTION_DOWN ) {
            children.forEach { it ->
                if(it is LinearLayout) {
                    it.forEach { it ->
                        if (it is DrawBaseView) {
                            var pt = IntArray(2)
                            it.getLocationOnScreen(pt)
                            if(ev.rawX >= pt[0] && ev.rawX <= (pt[0] + it.width) && ev.rawY >= pt[1] && ev.rawY <= (pt[1] + it.height)) {
                                it.onChoiceListener?.onChoice(it)
                            }
                        }
                    }
                }
            }
        }
//        var type = ev?.getToolType(0)
//        if(type == TOOL_TYPE_STYLUS) {
//            return false
//        }

        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
//        var type = ev?.getToolType(0)
//        if(type == TOOL_TYPE_STYLUS) {
//            return false
//        }
        return super.onTouchEvent(ev)
    }

}

/*
    提供用笔书写的视图组件，通过onTouchEvent事件里面的参数，实现坐标以及压感的书写
 */
open class DrawTouchView(context: Context?, attrs: AttributeSet?) : DrawView(context, attrs) {
    private val Tag : String = "TouchDrawView"
    private val minPressure = 0.1
    private val maxPressure = 0.9

    // Define a enum class about TouchEvent status
    enum class TouchStatus(i: Int) {
        NONE(0),
        DOWN(1),
        DOWN_MOVE(2),
        UP(3),
        UP_MOVE(4)
    }
    class DrawTouchObj {
        var status: TouchStatus? = null
        var pt: PointEx? = null
    }
    private class ObserverThread : Thread() {
        var observer: Observer<DrawTouchObj>? = null
        private var objList = LinkedList<DrawTouchObj>()
        fun publish(o: DrawTouchObj) {
            synchronized(objList) {
                objList.add(o)
            }
        }
        override fun run() {
            while(true) {
                if(observer == null) {
                    return
                }
                try {
                    //var curTm: Long = SystemClock.elapsedRealtime()
                    if (objList.size > 0) {
                        var obj: DrawTouchObj? = null
                        synchronized(objList) {
                            obj = objList.removeFirst()
                        }
                        obj?.let {
                            observer!!.onChanged(it)
                        }
//                        var diff = SystemClock.elapsedRealtime() - curTm
//                        Log.d("ObserverThread", "observerlist  diff=${diff}")
                    }
                    sleep(15)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }
    //表示触摸的状态
    private var touchStatus = TouchStatus.NONE
    private var touchObjList = LinkedList<DrawTouchObj>()
    private var observerList = ArrayList<ObserverThread>()
    var enableFingerDraw: Boolean = false
    var startDrawTm: Long? = null

    init {
        var thr = Thread {
            while(true) {
                try {
                    //var curTm: Long = SystemClock.elapsedRealtime()
                    if (touchObjList.size > 0) {
                        var obj: DrawTouchObj? = null
                        synchronized(touchObjList) {
                            obj = touchObjList.removeFirst()
                        }
                        obj?.let {
                            synchronized(observerList) {
                                observerList.forEach { action -> action.publish(it) }
                            }
                        }
//                        var diff = SystemClock.elapsedRealtime() - curTm
//                        Log.d("DrawTouchView", "touchObjList  diff=${diff}")
                    }
                    Thread.sleep(15)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        //thr.priority = Thread.MAX_PRIORITY
        thr.start()
    }
    private fun registerMyDrawObserver(observer: (obj: DrawTouchObj) -> Unit) {
        synchronized(observerList) {
            observerList.add(ObserverThread().apply {
                this.observer = Observer<DrawTouchObj>(observer)
                start()
                priority = Thread.MAX_PRIORITY
            })
        }
    }
    /*
        通过观察者模式来实现数据更新通知
     */
    fun registerDrawObserverFun(observer: (obj: DrawTouchObj) -> Unit) {
        synchronized(observerList) {
            observerList.add(ObserverThread().apply {
                this.observer = Observer<DrawTouchObj>(observer)
                start()
            })
        }
    }
    private fun postTouchObj(obj: DrawTouchObj) {
        handleMyTouchObj(obj)
        synchronized(touchObjList) {
            touchObjList.add(obj)
        }
    }

    override fun clearAll() {
        synchronized(touchObjList) {
            touchObjList.clear()
        }
        super.clearAll()
    }
    /*
        触摸事件
        在这里获取笔划过的轨迹坐标

     */


    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var x = event.x
        var y = event.y
        var z = event.pressure
        if(z < minPressure && event.action == MotionEvent.ACTION_MOVE) {
            return true
        }
        if(x == lastX && y == lastY && z == lastZ && event.action == MotionEvent.ACTION_MOVE) {
            return true
        }
        lastX = x
        lastY = y
        lastZ = z
        var pt = makePointEx(x, y, z, enableFingerDraw)
        var type = event.getToolType(0)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if(startDrawTm == null) {
                    startDrawTm = System.currentTimeMillis()
                }
                if(enableFingerDraw || type == TOOL_TYPE_STYLUS) {
                    touchStatus = TouchStatus.DOWN
                    postTouchObj(makeDrawStatusObj(TouchStatus.DOWN, pt))
                }
            }
            MotionEvent.ACTION_UP -> {
                if(enableFingerDraw || type == TOOL_TYPE_STYLUS) {
                    touchStatus = TouchStatus.UP
                    postTouchObj(makeDrawStatusObj(TouchStatus.UP, pt))
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                touchStatus = TouchStatus.NONE
                postTouchObj(makeDrawStatusObj(TouchStatus.NONE, pt))
            }
            MotionEvent.ACTION_MOVE -> {
                if(touchStatus == TouchStatus.DOWN) {
                    touchStatus = TouchStatus.DOWN_MOVE
                } else if(touchStatus == TouchStatus.UP) {
                    touchStatus = TouchStatus.UP_MOVE
                }
                if(touchStatus == TouchStatus.DOWN_MOVE
                    && (enableFingerDraw || type == TOOL_TYPE_STYLUS)) {
                    postTouchObj(makeDrawStatusObj(touchStatus, pt))
                }
            }
        }
        return true //super.onTouchEvent(event)
    }

    private fun makeDrawStatusObj(status: TouchStatus, pt: PointEx): DrawTouchObj {
        return DrawTouchObj().apply {
            this.status = status
            this.pt = pt
        }
    }

    private fun handleMyTouchObj(obj: DrawTouchObj) {
        when(obj.status) {
            TouchStatus.NONE -> {

            }
            TouchStatus.DOWN -> {
                onChoiceListener?.onChoice(this)
                readyDraw()
                obj.pt?.let {
                    drawPointEx(it, true)
                    //inDrawing(it)
                }
            }
            TouchStatus.DOWN_MOVE -> {
                obj.pt?.let {
                    drawPointEx(it,  false)
                    inDrawing(it)
                }
            }
            TouchStatus.UP -> {
                obj.pt?.let {
                    drawEndPointEx(it)
                    endDraw(it, ptLst)
                    ptLst.clear()
                }
            }
        }
    }
    protected open fun readyDraw() {

    }
    protected open fun inDrawing(pt: PointEx) {

    }
    protected open fun endDraw(pt:PointEx, pts: ArrayList<PointEx>) {

    }

    /*
        重载函数
        被父类DrawBaseView调用
        参数 canvas不是bmCanvas而是父类的surfaceView的canvas
     */
    override fun doDraw(canvas: Canvas?) {
        //调用父类doDraw
        super.doDraw(canvas)
    }
}