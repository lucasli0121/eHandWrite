package com.handwrite.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import com.handwrite.MainActivity
import kotlin.Exception

open class BaseFragment : Fragment() {
    private var isReady: Boolean = false
    private var isLoad: Boolean = false
    private var dataDeq: ArrayDeque<Any> = ArrayDeque()
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        isReady = true
        isLoad = true
        Thread{
            handleDataThread()
        }.start()
    }

    fun putDataList(datas: ArrayList<Any>) {
        dataDeq.addAll(datas)
    }

    private fun handleDataThread() {
        while (isReady) {
            try {
                if (isLoad && dataDeq.size > 0) {
                    var data = dataDeq.removeFirst()
                    Handler(Looper.getMainLooper()).post{onLazyLoad(data)}
                }
                Thread.sleep(100)
            } catch (e: Exception) {

            }
        }
    }

    override fun onDestroyView() {
        isReady = false
        super.onDestroyView()
    }
    fun onVisibilityChanged(visible: Boolean) {
        isLoad = visible && isReady
    }

    open fun onLazyLoad(obj: Any) {

    }
}