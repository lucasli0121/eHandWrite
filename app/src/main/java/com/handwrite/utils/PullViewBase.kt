package com.handwrite.utils

import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import com.handmark.pulltorefresh.library.PullToRefreshBase
import com.handmark.pulltorefresh.library.PullToRefreshListView

abstract class PullViewBase(context: Context?, v: PullToRefreshListView?) {
    var _refreshView: PullToRefreshListView? = null
    var _context: Context? = null
    var _realListView: ListView? = null
    init {
        _refreshView = v
        _context = context
        _refreshView!!.setOnRefreshListener { pullToRefreshBase ->
            GetDataTask().execute()
        }
        _refreshView!!.setOnPullEventListener { pullToRefreshBase, state, mode ->
            var label = makePullLabel()
            pullToRefreshBase.loadingLayoutProxy.setLastUpdatedLabel(label)
        }
        _realListView = _refreshView!!.refreshableView
    }
    open fun makePullLabel() : String {
        var label = DateUtils.formatDateTime(
            _context,
            System.currentTimeMillis(),
            DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_ALL
        )
        return label
    }
    abstract fun refreshObjs()
    inner class GetDataTask : AsyncTask<Void?, Void?, Array<Any>?>() {
        override fun doInBackground(vararg params: Void?): Array<Any>? {
            try {
                Handler(Looper.getMainLooper()).post{refreshObjs()}
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Array<Any>?) {
            _refreshView!!.onRefreshComplete()
            super.onPostExecute(result)
        }
    }
}
