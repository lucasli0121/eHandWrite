package com.handwrite.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.handwrite.MainApp
import com.handwrite.R
import com.handwrite.activity.DescActivity
import com.handwrite.databinding.ExerciseTipFragmentBinding
import com.handwrite.utils.PromptMessage
import com.handwrite.utils.ToolBox
import com.libwriting.dao.Article
import com.libwriting.dao.DaoBase
import com.libwriting.dao.TrackPoint
import com.libwriting.ui.DrawBaseView
import com.libwriting.ui.paper.PaperBaseView

class ExerciseFragment : BaseFragment(), ToolBox.OnToolBoxClickListener {
    lateinit var binding: ExerciseTipFragmentBinding
    var ttf: DaoBase.TtfType = DaoBase.TtfType.Kai
    var hasCreated = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var v = inflater.inflate(R.layout.exercise_tip_fragment, container, false)
        binding = ExerciseTipFragmentBinding.bind(v)
        binding.toolBox.listener = this
        binding.toolBox.setDefaultPenSize(DrawBaseView.minThick)
        binding.submitBut.setOnClickListener { onSubmitClick() }
        binding.toolBox.showRecordBut(false)
        binding.toolBox.setDefaultPenSize(DrawBaseView.minThick)
        binding.vTiaoPaper.isVTiaoxing = true
        binding.hTiaoPaper.isVTiaoxing = false
        hasCreated = true
        return v
    }

    private fun paper(): PaperBaseView? {
        var paper: PaperBaseView? = null
        if(binding.tianziPaper.isVisible) {
            paper = binding.tianziPaper
        } else if(binding.vTiaoPaper.isVisible) {
            paper = binding.vTiaoPaper
        } else if(binding.hTiaoPaper.isVisible) {
            paper = binding.hTiaoPaper
        }
        return paper
    }

    private fun clearWrite() {
        paper()?.clearAll()
    }
    private fun onRedo() {
        paper()?.captureView?.redo()
    }

    /*
        整篇提交
     */
    private fun onSubmitClick() {
        var paper: PaperBaseView? = paper()
        if(paper != null) {
            if(!paper.captureView.hasDirty) {
                PromptMessage.displayToast(context, "请写书写字才可以提交")
                return
            }
            var intent = Intent(context, DescActivity().javaClass)
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1) {
            var obj: Article = Article()
            if(resultCode == Activity.RESULT_OK) {
                obj.title = data?.getStringExtra("desc").toString()
            }
            var paper: PaperBaseView? = paper()
            if(paper != null) {
                var app: MainApp = context?.applicationContext as MainApp
                obj.userId = app.teacher.id
                obj.degree = paper?.captureView?.thick.toInt()
                obj.ttf = ttf.name
                obj.points = paper?.captureView?.formatPathToStr().toString()
                paper?.captureView?.getBitmap()?.let { obj.putPic(context!!, it) }
                obj.cols = paper?.captureView?.cols!!
                obj.rows = paper?.captureView?.rows!!
                obj.gridWidth = paper?.gridWidth.toFloat()
                obj.gridHeight = paper?.gridHeight.toFloat()
                obj.gridType = paper?.playView.gridType.ordinal
                obj.submit(context, object: DaoBase.OnSubmitResponse{
                    override fun onSuccess(code: Int, msg: String?, taskId: Int?) {
                        PromptMessage.displayToast(context, msg)
                        clearWrite()
                    }

                    override fun onFailure(error: String?, taskId: Int?) {
                        PromptMessage.displayToast(context, error)
                    }

                },1)
            }
        }
    }

    override fun onKeiButClick() {
        ttf = DaoBase.TtfType.Kai
    }

    override fun onXingButClick() {
        ttf = DaoBase.TtfType.Xing
    }

    override fun onThinButClick(res: Float) {
        paper()?.captureView?.thick = res
        paper()?.playView?.thick = res
    }

    override fun onClearClick() {
        clearWrite()
    }

    override fun onRedoButClick() {
        onRedo()
    }

    override fun onRecordButClick() {
    }
    override fun onTianziGridClick() {
        binding.tianziPaper.visibility = View.VISIBLE
        binding.vTiaoPaper.visibility = View.GONE
        binding.hTiaoPaper.visibility = View.GONE
    }
    override fun onHTiaoGridClick() {
        binding.tianziPaper.visibility = View.GONE
        binding.vTiaoPaper.visibility = View.VISIBLE
        binding.hTiaoPaper.visibility = View.GONE
    }
    override fun onVTiaoGridClick() {
        binding.tianziPaper.visibility = View.GONE
        binding.vTiaoPaper.visibility = View.GONE
        binding.hTiaoPaper.visibility = View.VISIBLE
    }
}