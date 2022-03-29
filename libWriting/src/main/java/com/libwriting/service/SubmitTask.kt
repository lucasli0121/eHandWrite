package com.libwriting.service

import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.libwriting.dao.DaoBase
import com.libwriting.utils.AppNotify
import com.write.libwriting.R
import java.util.*

class SubmitTask() {
    private class TaskObj {
        var channelId: Int = 0
        var obj: DaoBase? = null
    }
    private var context: Context? = null
    private val submitObjList = LinkedList<TaskObj>()
    private var uploadConnect: UploadService.UploadServiceConnect = UploadService.UploadServiceConnect()
    private var notify: AppNotify? = null
    private var index: Int = 1
    private var thrGroup = ThreadGroup("SubmitTask")
    private val workStackSize: Long = 5000 * 1024
    private var workThr: Thread? = null

    fun beginTask(ctx: Context) {
        context = ctx
        context?.let{notify = AppNotify(it)}
        bindUploadService() // bind 一个上传文件得Service
        // 启动一个任务线程，设定大的堆栈
        workThr = Thread(thrGroup, { handleThreadRunner() }, "submit", workStackSize )
        workThr?.start()
    }
    fun stopTask() {
        context?.unbindService(uploadConnect)
        workThr = null
    }

    companion object {
        private var ins: SubmitTask? = null
        fun get(): SubmitTask {
            if(ins == null) {
                ins = SubmitTask()
            }
            return ins as SubmitTask
        }
    }

    /*
        增加一个提交任务
     */
    fun postTask(obj: DaoBase) {
        var taskObj = makeTaskObj(obj)
        synchronized(submitObjList) {
            submitObjList.add(taskObj)
        }
    }
    private fun makeTaskObj(o: DaoBase): TaskObj {
        var id = index
        index += 2
        if(index == Int.MAX_VALUE) {
            index = 1
        }
        return TaskObj().apply {
            channelId = id
            obj = o
        }
    }
    /*
        绑定一个connect服务
     */
    private fun bindUploadService() {
        try {
            //设置回调，显示上传文件的信息
            uploadConnect.upCallback = object: UploadService.UploadCallbackInterface {
                override fun onUpload(
                    fileName: String?,
                    context: Context?,
                    total: Long,
                    value: Long,
                    taskId: Int?
                ) {
                    if(value < total) {
                        taskId?.let{ notify?.notifyProgress(it, value.toInt(), total.toInt()) }
                    } else {
                        taskId?.let{ notify?.delNotify(it) }
                    }
                }

                override fun onFailed(context: Context?, reason: String?, taskId: Int?) {
                    taskId?.let{notify?.delNotify(it)}
                }

            }
            var intent = Intent(context, UploadService().javaClass)
            context?.bindService(intent, uploadConnect, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun handleThreadRunner() {
        while(true) {
            try {
                if (submitObjList.size > 0) {
                    var taskObj: TaskObj? = null
                    synchronized(submitObjList) {
                        taskObj = submitObjList.removeFirst()
                    }
                    if(taskObj != null) {
                        uploadConnect.upService?.let { it: UploadService ->
                            /*notify?.makeNotify(
                                taskObj!!.channelId,
                                context?.resources?.getString(R.string.notify_content)
                            )*/
                            taskObj!!.obj?.submit(context, it, object : DaoBase.OnSubmitResponse {
                                override fun onSuccess(code: Int, msg: String?, taskId: Int?) {
                                    taskId?.let { it: Int ->
                                        /*notify?.makeNotify(
                                            it,
                                            context?.resources?.getString(R.string.submit_success)
                                        )*/
                                    }
                                }

                                override fun onFailure(error: String?, taskId: Int?) {
                                    taskId?.let { it: Int ->
                                    /*    notify?.makeNotify(
                                            it,
                                            context?.resources?.getString(R.string.submit_failed)
                                        )*/
                                    }
                                }
                            }, taskObj!!.channelId)
                        }
                    }
                    Thread.sleep(500)
                } else {
                    Thread.sleep(3 * 1000)
                }
            } catch (e: Exception) {
            }
        }
    }
}