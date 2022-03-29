package com.handwrite

import com.libwriting.WriteApp
import com.libwriting.dao.User
import com.tencent.bugly.crashreport.CrashReport

class MainApp: WriteApp() {
    init {
//        this.teacher = User().apply {
//            id = "8ae95ca3317e11ec87ba3a40a64fbde8"
//            name = "周老师"
//            mobile = "13425781806"
//        }
        this.teacher = User().apply {
            id = "322e1745574d11ec8ee0b2d0bd57a3a1"
            name = "周老师2"
            mobile = "13600012345"
        }
//        this.teacher = User().apply {
//            id = "184871615f1011ec8ee0b2d0bd57a3a1"
//            name = "张老师"
//            mobile = "13600012345"
//        }
//        this.teacher = User().apply {
//            id = "06d1c7f0402b11ec87ba3a40a64fbde8"
//            name = "刘老师"
//            mobile = "13425781806"
//        }
    }
    override fun onCreate() {
        super.onCreate()
        CrashReport.initCrashReport(applicationContext, "c23ecb62cb", true)
    }
}