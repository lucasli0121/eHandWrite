package com.handwrite.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.handwrite.R
import com.handwrite.databinding.ToolBoxBinding
import com.libwriting.ui.DrawBaseView

class ToolBox(context: Context, attrs: AttributeSet?) :  LinearLayout(context, attrs), ImageViewDrag.OnDragEvent{
    private var binding: com.handwrite.databinding.ToolBoxBinding
    var listener: OnToolBoxClickListener? = null
    var penThickValue: Float = DrawBaseView.minThick
    init {
        var inflater = LayoutInflater.from(context)
        var v = inflater.inflate(R.layout.tool_box, this, true)
        binding = ToolBoxBinding.bind(v)
        orientation = VERTICAL
        binding.kaiBut.typeface = Typeface.createFromAsset(context?.assets, context.resources.getString(
            com.write.libwriting.R.string.kai_font))
        binding.xingBut.typeface = Typeface.createFromAsset(context?.assets, context.resources.getString(
            com.write.libwriting.R.string.xing_font))
        binding.kaiBut.setOnClickListener { listener?.onKeiButClick() }
        binding.xingBut.setOnClickListener { listener?.onXingButClick() }
        binding.kaiBut.isChecked = true
        binding.xingBut.isChecked = false
        binding.pen1.setOnClickListener { handlePenClick(DrawBaseView.minThick) }
        binding.pen2.setOnClickListener { handlePenClick(DrawBaseView.midThick) }
        binding.pen3.setOnClickListener { handlePenClick(DrawBaseView.largeThick) }
        binding.emptyBut.setOnClickListener{ listener?.onClearClick() }
        binding.redoBut.setOnClickListener { listener?.onRedoButClick() }
        binding.tianziGrid.setOnClickListener { listener?.onTianziGridClick() }
        binding.htiaoGrid.setOnClickListener { listener?.onHTiaoGridClick() }
        binding.vtiaoGrid.setOnClickListener { listener?.onVTiaoGridClick() }
        binding.recordBut.setOnClickListener{listener?.onRecordButClick()}
        binding.dragView.onDragEvent = this
        handlePenClick(penThickValue)
    }

    fun setDefaultPenSize(penSize: Float) {
        handlePenClick(penSize)
    }
    private fun handlePenClick(penSize: Float) {
        penThickValue = penSize
        when(penSize) {
            DrawBaseView.minThick -> {
                binding.pen1.setImageResource( R.mipmap.pen_size1_sel)
                binding.pen2.setImageResource( R.mipmap.pen_size2)
                binding.pen3.setImageResource( R.mipmap.pen_size3)
            }
            DrawBaseView.midThick -> {
                binding.pen1.setImageResource( R.mipmap.pen_size1)
                binding.pen2.setImageResource( R.mipmap.pen_size2_sel)
                binding.pen3.setImageResource( R.mipmap.pen_size3)
            }
            DrawBaseView.largeThick -> {
                binding.pen1.setImageResource( R.mipmap.pen_size1)
                binding.pen2.setImageResource( R.mipmap.pen_size2)
                binding.pen3.setImageResource( R.mipmap.pen_size3_sel)
            }
        }
        listener?.onThinButClick(penSize)
    }
    fun showGridButs(show: Boolean) {
        binding.tianziGrid.visibility = if(show) View.VISIBLE else View.GONE
        binding.htiaoGrid.visibility = if(show) View.VISIBLE else View.GONE
        binding.vtiaoGrid.visibility = if(show) View.VISIBLE else View.GONE
        binding.gridSet.visibility = if(show) View.VISIBLE else View.GONE
    }
    fun showRecordBut(show: Boolean) {
        binding.recordBut.visibility = if(show) View.VISIBLE else View.GONE
    }

    interface OnToolBoxClickListener {
        fun onKeiButClick()
        fun onXingButClick()
        fun onThinButClick(res: Float)
        fun onClearClick()
        fun onRedoButClick()
        fun onRecordButClick()
        fun onTianziGridClick()
        fun onHTiaoGridClick()
        fun onVTiaoGridClick()
    }

    fun modifyRecordButIcon(iconId: Int) {
        binding.recordBut.setImageResource(iconId)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    override fun beginDrag(image: ImageViewDrag?, dx: Float, dy: Float) {
        this.translationX += dx
        this.translationY += dy
    }

    override fun endDrag(image: ImageViewDrag?, rawX: Float, rawY: Float) {
    }
}