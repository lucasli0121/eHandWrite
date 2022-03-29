package com.libwriting.ui

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import com.libwriting.dao.TrackPoint
import com.libwriting.giftools.GifMake
import com.write.libwriting.R
import com.write.libwriting.databinding.CapturePlayBinding

/*
    底部增加动态显示轨迹
    上层增加采集写字组件

 */
open class CapturePlayView(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    private val Tag : String = "CapturePlayView"
    private var binding: CapturePlayBinding
    var playView: PlayView
    var captureView: CaptureTouchView
    init {
        var inflater = LayoutInflater.from(context)
        var v = inflater.inflate(R.layout.capture_play, this, true)
        binding = CapturePlayBinding.bind(v)
        playView = binding.play
        captureView = binding.capture
        binding.capture.backColor = Color.TRANSPARENT
        binding.capture.gridType = DrawBaseView.GridType.NoneType
        binding.capture.penColor = Color.RED
        binding.play.penColor = resources.getColor(R.color.light_gray, null)
    }
    fun clearAll() {
        synchronized(this) {
            binding.play.stopPlay()
            binding.play.clearAll()
            binding.capture.clearAll()
        }
    }

}