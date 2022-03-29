package com.libwriting.ui.back

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.widget.ImageViewCompat
import com.write.libwriting.R

class ElliBackgroud(context: Context?, attrs: AttributeSet?) : androidx.appcompat.widget.AppCompatImageView(context!!, attrs) {

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

    override fun onDraw(canvas: Canvas?) {
        drawImg(canvas)
        super.onDraw(canvas)
    }

    private fun drawImg(canvas: Canvas?) {
        canvas?.let {
            val paint = Paint()
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.flags = Paint.ANTI_ALIAS_FLAG
            paint.color = backColor
            it.drawOval(RectF().apply {
                this.left = 0f
                this.top = 0f
                this.right = this@ElliBackgroud.width.toFloat()
                this.bottom = this@ElliBackgroud.height.toFloat()
            }, paint)
            paint.style = Paint.Style.STROKE
            paint.color = penColor
            it.drawOval(RectF().apply {
                this.left = 2f
                this.top = 2f
                this.right = this@ElliBackgroud.width.toFloat() - 2f
                this.bottom = this@ElliBackgroud.height.toFloat() - 2f
            }, paint)
            it.drawOval(RectF().apply {
                this.left = 4f
                this.top = 4f
                this.right = this@ElliBackgroud.width.toFloat() - 4f
                this.bottom = this@ElliBackgroud.height.toFloat() - 4f
            }, paint)
        }
    }
}