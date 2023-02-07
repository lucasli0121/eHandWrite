package com.libwriting.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import com.libwriting.R

class RecordImg(context: Context, attrs: AttributeSet?): androidx.appcompat.widget.AppCompatImageView(context, attrs) {
    var recording: Boolean = false
    private var defColor = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)  resources.getColor(R.color.light_gray, null) else resources.getColor(R.color.light_gray)
    private var recordColor = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) resources.getColor(R.color.red, null) else resources.getColor(R.color.red)
    private var flash: Boolean = false
    init {
        Thread {
            while(true) {
                if(recording) {
                    flash = !flash
                    postInvalidate()
                    Thread.sleep(300)
                } else {
                    Thread.sleep(1000)
                }

            }
        }.start()
    }
    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            val paint = Paint()
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.flags = Paint.ANTI_ALIAS_FLAG
            if(recording && flash) {
                paint.color = recordColor
            } else {
                paint.color = defColor
            }
            var cx = left + width / 2
            var cy = top + height / 2
            it.drawCircle(cx.toFloat(), cy.toFloat(), 5f, paint)
        }
    }
}