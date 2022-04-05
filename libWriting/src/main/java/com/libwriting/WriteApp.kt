package com.libwriting

import android.app.Application

open class WriteApp: Application() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}