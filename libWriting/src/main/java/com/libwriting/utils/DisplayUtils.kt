package com.libwriting.utils

import android.R
import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup

object DisplayUtils {
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(context: Context, pxValue: Float): Int {
        var dm = DisplayMetrics()
        dm = context!!.resources.displayMetrics
        val scale = dm.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun getRootView(context: Activity): View {
        return (context.findViewById<View>(R.id.content) as ViewGroup).getChildAt(0)
    }
}