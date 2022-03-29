package com.handwrite.fragment

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.handmark.pulltorefresh.library.PullToRefreshBase
import com.handmark.pulltorefresh.library.PullToRefreshListView
import com.handwrite.MainApp
import com.handwrite.R
import com.handwrite.databinding.RecordFragmentBinding
import com.handwrite.databinding.RecordListItemBinding
import com.handwrite.utils.PromptMessage
import com.handwrite.utils.PullViewBase
import com.libwriting.dao.Record
import com.libwriting.dao.Word
import com.libwriting.ui.DrawBaseView
import java.net.URL

class RecordFragment : BaseFragment() {
    lateinit var binding: RecordFragmentBinding
    lateinit var pullView: RecordPullView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v = inflater.inflate(R.layout.record_fragment, container, false)
        binding = RecordFragmentBinding.bind(v)
        binding.closeImg.setOnClickListener {
            binding.playView.clearAll()
            binding.showPic.visibility = View.GONE
        }
        pullView = RecordPullView(inflater.context, binding.pullRefresh)
        binding.pullRefresh.mode = PullToRefreshBase.Mode.BOTH
        pullView.recordView = this
        binding.playView.playView.gridType = DrawBaseView.GridType.MiziType
        binding.playView.playView.gridLineDefColor = resources.getColor(R.color.red, null)
        binding.playView.playView.gridLineChoiceColor = resources.getColor(R.color.red, null)
        binding.playView.playView.backColor = resources.getColor(android.R.color.white, null)
        binding.playView.playView.penColor = Color.CYAN
        return v
    }
    fun showBitmap(picUrl: String?) {
        Thread{
            try {
                var url = URL(picUrl)
                var bm = BitmapFactory.decodeStream(url.openStream())
                Handler(Looper.getMainLooper()).post {
                    binding.showPic.visibility = View.VISIBLE
                    binding.playView.playView.resetBitmap(bm)
                }
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

    }
    fun showPoints(points: String?) {
        if (points != null) {
            binding.showPic.visibility = View.VISIBLE
            binding.playView.playView.drawPathFromStr(points)
        }
    }

}

class RecordPullView(context: Context?, v: PullToRefreshListView?) : PullViewBase(context, v) {
    var adapter: Adapter = context?.let { Adapter(it) }!!
    var recordView: RecordFragment? = null
    init {
        _context = context
        _realListView?.adapter = adapter
        adapter.pullView = this
        refreshObjs()
    }
    override fun makePullLabel(): String {
        var label = if(_refreshView?.currentMode == PullToRefreshBase.Mode.PULL_FROM_START) {
            var num = if(Record.pageNum > 1) Record.pageNum - 1 else 1
            "刷新页 $num / ${Record.maxPage}"
        } else {
            var num = if(Record.pageNum < Record.maxPage) Record.pageNum + 1 else Record.maxPage
            "刷新页 $num / ${Record.maxPage}"
        }
        return label
    }
    override fun refreshObjs() {
        class Response: Record.RecordResponse {
            override fun onSuccess(results: ArrayList<Record>?) {
                this@RecordPullView.adapter.clear()
                results?.let{this@RecordPullView.adapter.addAll(it)}
                this@RecordPullView.adapter.notifyDataSetChanged()
            }
            override fun onFailure(error: String?) {
                PromptMessage.displayToast(_context, error)
            }
        }
        var app: MainApp = _context?.applicationContext as MainApp
        var userId: String = app.teacher.id
        if(_refreshView?.currentMode == PullToRefreshBase.Mode.PULL_FROM_END) {
            Record.queryPageDown(userId, Response())
        } else if(_refreshView?.currentMode == PullToRefreshBase.Mode.PULL_FROM_START) {
            Record.queryPageUp(userId, Response())
        }
    }
    fun showPic(points: String?) {
        recordView?.showPoints(points)
    }
    fun deleteRecord(pos: Int, traceId: String?) {
        _context?.let {
            PromptMessage.showMessageDlgWithCancel(
                it,
                R.string.delete_prompt,
                { dialog, which -> deleteSureRecord(pos, traceId) },
                { dialog, which -> return@showMessageDlgWithCancel }
            )
        }
    }
    private fun deleteSureRecord(pos: Int, traceId: String?) {
        var rec = Record()
        rec.traceId = traceId
        rec.deleteRecord(object: Record.RecordResponse {
            override fun onSuccess(results: ArrayList<Record>?) {
                adapter.remove(adapter.getItem(pos))
            }
            override fun onFailure(error: String?) {
                PromptMessage.displayToast(_context, error)
            }

        })
    }
}


class Adapter(context: Context): ArrayAdapter<Record>(context, R.layout.record_list_item) {
    var pullView: RecordPullView? = null
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        if (v == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = inflater.inflate(R.layout.record_list_item, parent, false)
        }
        var obj  = getItem(position) as Record
        var binding: RecordListItemBinding? = v?.let { RecordListItemBinding.bind(it) }
        binding?.text?.text = obj.text
        Thread{
            try {
                var url = URL(obj.pic)
                var img = BitmapFactory.decodeStream(url.openStream())
                Handler(Looper.getMainLooper()).post {
                    binding?.trackPic?.setImageBitmap(img)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
        binding?.trackPic?.setOnClickListener { pullView?.showPic(obj.points) }
        binding?.createTm?.text = obj.createTime
        binding?.imgDelete?.setOnClickListener { pullView?.deleteRecord(position, obj.traceId) }
        return v!!
    }
}