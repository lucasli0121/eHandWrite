package com.libwriting.giftools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.os.Environment
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/*
    创建GIF
    把外部传递的bitmap写到一个gif bytearray中
    最后可以一次从函数获取整个gif文件
 */
class GifMake(ctx: Context?, w: Int, h: Int) {
    private val Tag = "GifMake"
    private class FrameOfBitmap {
        var index: Int = 0
        var delay: Long = 0
        var frame: Bitmap? = null
    }
    private var width = w
    private var height = h
    // 为了合成gif，需要定义一个list，用来保存每一帧的bitmap(每一帧指的是每一个坐标点)
    private var frameList = ArrayList<FrameOfBitmap>()
    private var context: Context? = ctx

    init {

    }

    /*
        clone 一个对象
     */
    fun clone(): GifMake {
        var vv = GifMake(context, width, height).apply {
            this.frameList.addAll(this@GifMake.frameList)
        }
        return vv
    }

    fun redo(index: Int) {
        // 回退framelst中对应的bitmap
        frameList.forEach {
            if (it.index == index) {
                it.frame?.recycle()
            }}
        frameList.removeIf { it.index == index }
    }

    fun clear() {
        frameList.forEach { it.frame?.recycle() }
        frameList.clear()
    }

    fun addFrame(bm: Bitmap?, d: Long, idx: Int) {
        bm?.let {
            var matrx = Matrix().apply {
                postScale(width.toFloat() / it.width.toFloat(), height.toFloat() / it.height.toFloat())
            }
            frameList.add(FrameOfBitmap().apply {
                index = idx
                delay = d
                frame = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrx, false)
            })
        }
    }

    fun makeGifFile(fileName: String): String? {
        if(frameList.size <= 0) {
            return null
        }
        var bmGif: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        var canvas: Canvas = Canvas(bmGif)
        val bos = ByteArrayOutputStream()
        var gifEncoder = AnimatedGifEncoderEx()
        gifEncoder.start(bos)
        gifEncoder.setTransparent(Color.BLACK)
        gifEncoder.setRepeat(0)
        bmGif.setHasAlpha(true)
        var i = 0
        frameList.forEach { t ->
            t.frame?.let {
                gifEncoder.setDelay(t.delay.toInt())
                canvas.drawBitmap(it, 0f, 0f, null)
//                var fileName = "${i}_test.png"
//                i++
//                var file = File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
//                if(!file.exists()) {
//                    file.createNewFile()
//                }
//                val os: OutputStream = FileOutputStream(file)
//                bmGif.compress(Bitmap.CompressFormat.PNG, 100, os)
//                os.flush()
//                os.close()
                gifEncoder.addFrame(bmGif.copy(bmGif.config, bmGif.isMutable), false)
            }
        }
        gifEncoder.finish()
        try {
            var file =
                File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
            file.deleteOnExit()
            file.createNewFile()
            var os: OutputStream = FileOutputStream(file)
            bos.writeTo(os)
            os.flush()
            os.close()
            bos.close()
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}