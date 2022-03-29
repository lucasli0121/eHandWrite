package com.libwriting.utils

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.io.*

class RecordDevice : Runnable {
    private val sampleRate = 8000
    private var record: AudioRecord? = null
    private var dataBuf: ByteArray? = null
    private var thread: Thread? = null
    private var bufSize = 640
    private var hasRecord = false
    var recordListener: OnRecordingListener? = null

    init {
        bufSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        dataBuf = ByteArray(bufSize)
    }

    interface OnRecordingListener {
        fun onRecording(data: ByteArray?, size: Int, escapetime: Long)
    }

    fun startRecord() {
        if (!hasRecord) {
            hasRecord = true
            record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufSize
            )
            thread = Thread(this)
            thread!!.name = "RecordDevice"
            thread!!.start()
        }
    }

    fun stopRecord() {
        thread?.let {
            hasRecord = false
            while (it.isAlive) {
                try {
                    Thread.sleep(100)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        thread = null
    }

    override fun run() {
        try {
            record?.let{
                val preSystemTime = System.currentTimeMillis()
                it.startRecording()
                while (hasRecord) {
                    val len = it.read(dataBuf!!, 0, bufSize)
                    val diffTime = System.currentTimeMillis() - preSystemTime
                    recordListener?.onRecording(dataBuf, len, diffTime)
                }
            }

        } catch (e: Throwable) {
            Log.e("WaveInDevice", "catch Expcetion", e)
        } finally {
            try {
                record?.let{
                    it.stop()
                    it.release()
                }
            } catch (e: Throwable) {
                Log.e("WaveInDevice", "catch Expcetion", e)
            }
            Log.i("WaveInDevice", "Exit Thread")
        }
    }

    fun Pcm2Wav(srcFileName: String?, destFileName: String?) {
        class WavHeader {
            var riff: String = "RIFF"
            var size: Int? = null
            var type: String = "WAVE"
        }

        class WavFmt {
            var fccID: String = "fmt "
            var size = 16
            var formatTag = 1
            var channels: Int? = null
            var samplesPerSec: Int? = null
            var avgBytesPerSec: Int? = null
            var blockAlign = 4
            var uiBitsPerSample: Int? = null
        }

        class WavData {
            var id: String = "data"
            var size: Int? = null
        }

        val header = WavHeader()
        val fmt = WavFmt()
        val data = WavData()
        fmt.samplesPerSec = sampleRate
        fmt.channels = 2
        fmt.uiBitsPerSample = 16
        fmt.avgBytesPerSec = fmt.samplesPerSec!! * 16 * fmt.channels!! / 8
        val srcFile = File(srcFileName)
        val srcFileLen = srcFile.length().toInt()
        data.size = srcFileLen
        header.size = 36 + data.size!!
        val destFile = File(destFileName)
        if (destFile.exists()) {
            destFile.delete()
        }
        try {
            val fos = FileOutputStream(destFile)
            val bos = BufferedOutputStream(fos)
            val dos = DataOutputStream(bos)
            val fin = FileInputStream(srcFile)
            dos.write(header.riff.toByteArray())
            dos.writeByte(header.size!! and 0xff)
            dos.writeByte(header.size!! shr 8 and 0xff)
            dos.writeByte(header.size!! shr 16 and 0xff)
            dos.writeByte(header.size!! shr 24 and 0xff)
            dos.write(header.type.toByteArray())
            dos.write(fmt.fccID.toByteArray())
            dos.writeByte(fmt.size and 0xff)
            dos.writeByte(fmt.size shr 8 and 0xff)
            dos.writeByte(fmt.size shr 16 and 0xff)
            dos.writeByte(fmt.size shr 24 and 0xff)
            dos.writeByte(fmt.formatTag and 0xff)
            dos.writeByte(fmt.formatTag shr 8 and 0xff)
            dos.writeByte(fmt.channels!! and 0xff)
            dos.writeByte(fmt.channels!! shr 8 and 0xff)
            dos.writeByte(fmt.samplesPerSec!! and 0xff)
            dos.writeByte(fmt.samplesPerSec!! shr 8 and 0xff)
            dos.writeByte(fmt.samplesPerSec!! shr 16 and 0xff)
            dos.writeByte(fmt.samplesPerSec!! shr 24 and 0xff)
            dos.writeByte(fmt.avgBytesPerSec!! and 0xff)
            dos.writeByte(fmt.avgBytesPerSec!! shr 8 and 0xff)
            dos.writeByte(fmt.avgBytesPerSec!! shr 16 and 0xff)
            dos.writeByte(fmt.avgBytesPerSec!! shr 24 and 0xff)
            dos.writeByte(fmt.blockAlign and 0xff)
            dos.writeByte(fmt.blockAlign shr 8 and 0xff)
            dos.writeByte(fmt.uiBitsPerSample!! and 0xff)
            dos.writeByte(fmt.uiBitsPerSample!! shr 8 and 0xff)
            dos.write(data.id.toByteArray())
            dos.writeByte(data.size!! and 0xff)
            dos.writeByte(data.size!! shr 8 and 0xff)
            dos.writeByte(data.size!! shr 16 and 0xff)
            dos.writeByte(data.size!! shr 24 and 0xff)
            val buf = ByteArray(srcFileLen)
            fin.read(buf, 0, srcFileLen)
            dos.write(buf)
            dos.flush()
            dos.close()
            fin.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}