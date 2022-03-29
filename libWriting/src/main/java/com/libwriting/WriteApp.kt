package com.libwriting

import android.app.Application
import com.libwriting.dao.User
import com.libwriting.service.SubmitTask

open class WriteApp: Application() {
    var teacher: User = User()
    override fun onCreate() {
        super.onCreate()
        SubmitTask.get().beginTask(applicationContext)
    }

    override fun onTerminate() {
        SubmitTask.get().stopTask()
        super.onTerminate()
    }
}