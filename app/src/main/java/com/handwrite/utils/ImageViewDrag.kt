package com.handwrite.utils

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.MotionEvent

/*
    此类是一个拖动的图片组件
    通过回调方式告诉外界，自己被拖动的情况，有开始拖动和结束拖动事件
 */
class ImageViewDrag(context: Context?, attrs: AttributeSet?) : AppCompatImageView( context!!, attrs) {
    private var isDown = false
    private var isDrag = false
    private var lastRawX = 0f
    private var lastRawY = 0f
    var onDragEvent: OnDragEvent? = null

    interface OnDragEvent {
        fun beginDrag(image: ImageViewDrag?, dx: Float, dy: Float)
        fun endDrag(image: ImageViewDrag?, rawX: Float, rawY: Float)
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.rawX
        val y = event.rawY
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDown = true
                lastRawX = x
                lastRawY = y
            }
            MotionEvent.ACTION_UP -> {
                if (onDragEvent != null && isDrag) {
                    onDragEvent?.endDrag(this, x, y)
                }
                isDown = false
                isDrag = false
            }
            MotionEvent.ACTION_CANCEL -> {
                if (onDragEvent != null && isDrag) {
                    onDragEvent?.endDrag(this, x, y)
                }
                isDrag = false
                isDown = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDown) {
                    isDrag = true
                }
                if (onDragEvent != null && isDrag) {
                    val dx = x - lastRawX
                    val dy = y - lastRawY
                    onDragEvent?.beginDrag(this, dx, dy)
                    lastRawX = x
                    lastRawY = y
                }
            }
        }
        return true //super.onTouchEvent(event);
    }
}