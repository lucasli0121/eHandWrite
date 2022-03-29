package com.libwriting.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.write.libwriting.R
import com.write.libwriting.databinding.AnimateFrameBinding

class AnimateWnd(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    var binding: AnimateFrameBinding
   // lateinit var gifDrawable: GifDrawable
    init {
        var inflater = LayoutInflater.from(context)
        var v = inflater.inflate(R.layout.animate_frame, this, true)
        binding = AnimateFrameBinding.bind(v)
    }
    fun showGifAnimate(fileName: String) {
       /* try {
            gifDrawable = GifDrawable(fileName)
            gifDrawable.loopCount = 1
            binding.animateImg.setImageDrawable(gifDrawable)
            gifDrawable.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }*/
    }

    fun replayAnimate() {
       /* try {
            gifDrawable?.stop()
            gifDrawable?.seekToFrame(0)
            gifDrawable?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }*/
    }
}