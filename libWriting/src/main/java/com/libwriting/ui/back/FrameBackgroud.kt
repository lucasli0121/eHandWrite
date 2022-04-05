package com.libwriting.ui.back

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.widget.ImageViewCompat
import com.write.libwriting.R

class FrameBackgroud(context: Context?, attrs: AttributeSet?) : androidx.appcompat.widget.AppCompatImageView(context!!, attrs) {

    var penColor: Int = resources.getColor(R.color.black, null)
        set(v) {
            if(field != v) {
                field = v
                invalidate()
                //postInvalidate()
            }
        }
    var backColor: Int = resources.getColor(android.R.color.transparent, null)
        set(value) {
            if(field != value) {
                field = value
                invalidate()
                //postInvalidate()
            }
        }

    init {
        var typeAry = context?.theme?.obtainStyledAttributes(attrs, R.styleable.FrameBackgroud, 0, 0)
        if(typeAry != null) {
            for( i in 0 until typeAry.indexCount) {
                var idx = typeAry.getIndex(i)
                when(idx ) {
                    R.styleable.FrameBackgroud_penColor -> {
                        penColor = typeAry.getColor(idx, Color.RED)
                        break
                    }
                    R.styleable.FrameBackgroud_backColor -> {
                        backColor = typeAry.getColor(idx, Color.TRANSPARENT)
                    }
                }

            }
            typeAry.recycle()
        }
    }
    override fun onDraw(canvas: Canvas?) {
        drawImg(canvas)
        super.onDraw(canvas)
    }

    private fun drawImg(canvas: Canvas?) {
        val paint = Paint()
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.flags = Paint.ANTI_ALIAS_FLAG
        if (canvas != null) {
            if(background == null) {
                paint.color = backColor
                canvas?.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
            } else {
                background.draw(canvas)
            }
        }

        paint.style = Paint.Style.STROKE
        paint.color = penColor
        paint.strokeWidth = 2f
        canvas?.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
    }
}