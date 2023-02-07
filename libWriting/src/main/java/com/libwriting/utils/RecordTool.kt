package com.libwriting.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.libwriting.R
import com.libwriting.databinding.RecordToolBinding
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream

class RecordTool(ctx: Context, attrs: AttributeSet): LinearLayout(ctx, attrs) {
    private var record = RecordDevice()
    private var fileName: String? = null
    private var binding: RecordToolBinding
    var isRecord: Boolean = false
    var dataOutStream: DataOutputStream? = null
    var startRecordTm = System.currentTimeMillis()

    init {
        var inflater = LayoutInflater.from(context)
        var v = inflater.inflate(R.layout.record_tool, this, true)
        binding = RecordToolBinding.bind(v)
        orientation = HORIZONTAL
        record.recordListener = object: RecordDevice.OnRecordingListener {
            override fun onRecording(data: ByteArray?, size: Int, escapetime: Long) {
                binding.recordImg.recording = true
                Handler(Looper.getMainLooper()).post{binding.escapeTm.text = (escapetime.toFloat() / 1000).toInt().toString()}
                data?.let{
                    try {
                        dataOutStream?.write(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun startRecord(): Boolean {
        try {
            if(!isRecord) {
                isRecord = true
                fileName = UUIDTool.getId()
                var file = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), fileName)
                file.deleteOnExit()
                file.createNewFile()
                dataOutStream = DataOutputStream(BufferedOutputStream(FileOutputStream(file)))
                startRecordTm = System.currentTimeMillis()
                record.startRecord()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }
    fun stopRecord() {
        try {
            if(isRecord) {
                isRecord = false
                record.stopRecord()
                dataOutStream?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun clearFile() {
        if(fileName != null) {
            try {
                File(fileName).deleteOnExit()
                var destFile = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath + "/$fileName.wav"
                File(destFile).deleteOnExit()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun convertToWav(): Pair<String?, String?> {
        if(fileName == null) {
            return Pair(null, null)
        }
        var newFile = "$fileName.wav"
        var srcFile = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath + "/" + fileName
        var destFile = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath + "/" + newFile
        record.Pcm2Wav(srcFile, destFile)
        var file = File(srcFile)
        file.deleteOnExit()
        return Pair(newFile, destFile)
    }
}