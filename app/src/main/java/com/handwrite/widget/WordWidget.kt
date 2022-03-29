package com.handwrite.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import com.handmark.pulltorefresh.library.PullToRefreshBase
import com.handmark.pulltorefresh.library.PullToRefreshListView
import com.handwrite.MainApp
import com.handwrite.R
import com.handwrite.databinding.WordListItemBinding
import com.handwrite.databinding.WordWidgetLayoutBinding
import com.handwrite.utils.PromptMessage
import com.handwrite.utils.PullViewBase
import com.libwriting.WriteApp
import com.libwriting.dao.DaoBase
import com.libwriting.dao.Word
import com.libwriting.ui.DrawBaseView
import com.libwriting.ui.course.WordView

class WordWidget(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs)  {
    private lateinit var binding: WordWidgetLayoutBinding
    private lateinit var pullView: WordPullView
    init {
        var inflater = LayoutInflater.from(context)
        var v = inflater.inflate(R.layout.word_widget_layout, this, true)
        binding = WordWidgetLayoutBinding.bind(v)
        binding.pullRefresh.mode = PullToRefreshBase.Mode.BOTH
        pullView = WordPullView(inflater.context, binding.pullRefresh)
    }
    fun setGradeId(gradeId: String) {
        pullView.gradeId = gradeId
        pullView.refreshObjs()
    }
    fun changeTtf(t: DaoBase.TtfType) {
        pullView.ttf = t
    }
    fun setWordStatus(wordId: String, hasWrite: Boolean) {
        var app = context.applicationContext as WriteApp
        for(i in 0 until pullView.adapter.count) {
            var word = pullView.adapter.getItem(i) as Word
            if(word?.id == wordId && hasWrite) {
                word?.userId = app.teacher.id
                word?.userName = app.teacher.name
                pullView.adapter.notifyDataSetChanged()
                break
            }
        }
    }
    fun setWordViewClickListener(v: OnWordViewClickListener) {
        pullView.adapter.onWordViewClickListener = v
    }
}

class WordPullView(context: Context?, v: PullToRefreshListView?) : PullViewBase(context, v) {
    var adapter: WordAdapter = context?.let { WordAdapter(it) }!!
    var gradeId: String = ""
    var ttf: DaoBase.TtfType = DaoBase.TtfType.Kai
    init {
        _context = context
        _realListView?.adapter = adapter
        adapter.pullView = this
    }

    override fun makePullLabel(): String {
        var label: String = ""
        if(_refreshView?.currentMode == PullToRefreshBase.Mode.PULL_FROM_START) {
            var num = if(Word.pageNum > 1) Word.pageNum - 1 else 1
            label = "刷新页 $num / ${Word.maxPage}"
        } else {
            var num = if(Word.pageNum < Word.maxPage) Word.pageNum + 1 else Word.maxPage
            label = "刷新页 $num / ${Word.maxPage}"
        }
        return label
    }
    override fun refreshObjs() {
        var app = _context?.applicationContext as MainApp
        if(_refreshView?.currentMode == PullToRefreshBase.Mode.PULL_FROM_END) {
            Word.queryPageDown(app.teacher.id, gradeId, object : Word.WordResponse {
                override fun onSuccess(results: ArrayList<Word>?) {
                    this@WordPullView.adapter.clear()
                    results?.let { this@WordPullView.adapter.addAll(it) }
                    this@WordPullView.adapter.notifyDataSetChanged()
                }

                override fun onFailure(error: String?) {
                    PromptMessage.displayToast(_context, error)
                }
            })
        } else if(_refreshView?.currentMode == PullToRefreshBase.Mode.PULL_FROM_START) {
            Word.queryPageUp(app.teacher.id, gradeId, object : Word.WordResponse {
                override fun onSuccess(results: ArrayList<Word>?) {
                    this@WordPullView.adapter.clear()
                    results?.let { this@WordPullView.adapter.addAll(it) }
                    this@WordPullView.adapter.notifyDataSetChanged()
                }

                override fun onFailure(error: String?) {
                    PromptMessage.displayToast(_context, error)
                }
            })
        }
    }

}

class WordAdapter(context: Context): ArrayAdapter<Word>(context, R.layout.word_list_item) {
    var pullView: WordPullView? = null
    var onWordViewClickListener: OnWordViewClickListener? = null
    private var lastChoiceObj: WordView? = null
    override fun clear() {
        if(lastChoiceObj != null) {
            lastChoiceObj?.choice = false
            lastChoiceObj = null
        }
        super.clear()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        if (v == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = inflater.inflate(R.layout.word_list_item, parent, false)
        }
        var obj = getItem(position) as Word
        var binding: WordListItemBinding? = v?.let{ WordListItemBinding.bind(it)}
        if(obj.hasWrite(context)) {
            binding?.statusInfo?.text = "${obj.userName} 已书写"
        } else {
            binding?.statusInfo?.text = "未书写"
        }
        binding?.wordView?.word = obj
        pullView?.ttf?.let { binding?.wordView?.changeTtf(it) }
        binding?.wordView?.gridType = DrawBaseView.GridType.TianziType
        binding?.wordView?.gridLineDefColor = context.resources.getColor(R.color.white, null)
        binding?.wordView?.gridLineChoiceColor = context.resources.getColor(R.color.red, null)
        binding?.wordView?.backColor = context.resources.getColor(android.R.color.darker_gray, null)
        binding?.wordView?.penColor = context.resources.getColor(R.color.white, null)
        binding?.wordView?.setOnClickListener { v ->
            if(lastChoiceObj != null) {
                lastChoiceObj?.choice = false
            }
            (v as WordView).choice = true
            onWordViewClickListener?.onWordClick(v as WordView)
            lastChoiceObj = v
        }
        return v!!
    }
}

interface OnWordViewClickListener {
    fun onWordClick(v: WordView)
}