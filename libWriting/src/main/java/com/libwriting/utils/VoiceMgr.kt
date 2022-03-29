package com.libwriting.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import java.io.File
import java.io.FileInputStream

class VoiceMgr {
    private var context: Context? = null
    private var mPlay: MediaPlayer? = null
    private var isStart = false
    val isPlay: Boolean
        get() = mPlay!!.isPlaying
    private var uri: Uri? = null

    constructor(ctx: Context?) {
        context = context
        uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        preparePlay()
    }

    constructor(ctx: Context?, u: Uri?) {
        context = context
        uri = u
        preparePlay()
    }
    constructor(ctx: Context, file: File) {
        var fin = FileInputStream(file)
        if (mPlay == null) {
            mPlay = MediaPlayer()
            try {
                mPlay!!.setDataSource(fin.fd)
                mPlay!!.setVolume(1000f, 1000f)
                mPlay!!.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun preparePlay() {
        if (mPlay == null) {
            mPlay = MediaPlayer()
            try {
                mPlay!!.setDataSource(context!!, uri!!)
                mPlay!!.setVolume(1000f, 1000f)
                mPlay!!.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /*
		开始播放语音
		参数：stime单位毫秒，用来控制播放时长，从开始播放到设置时长后就停止播放
	 */
    fun startPlay(stime: Int = 0) {
        if (!isStart) {
            isStart = true
            preparePlay()
            mPlay!!.seekTo(0)
            mPlay!!.start()
            if (stime > 0) {
                Thread {
                    try {
                        Thread.sleep(stime.toLong())
                    } catch (e: Exception) {
                    }
                    stopPlay()
                }.start()
            }
        }
    }

    fun stopPlay() {
        if (isStart) {
            mPlay!!.pause()
            mPlay!!.stop()
            mPlay!!.release()
            mPlay = null
            isStart = false
        }
    }
}