package com.handwrite.fragment

import android.content.Context
import android.graphics.*
import android.graphics.BitmapFactory.*
import android.os.*
import android.util.AttributeSet
import android.view.*
import android.widget.*
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.view.size
import com.handwrite.MainApp
import com.handwrite.R
import com.handwrite.databinding.TakeSampleFragmentBinding
import com.handwrite.utils.PromptMessage
import com.handwrite.utils.ToolBox
import com.handwrite.widget.OnWordViewClickListener
import com.handwrite.widget.WordWidget
import com.libwriting.dao.DaoBase
import com.libwriting.dao.Grade
import com.libwriting.dao.TrackPoint
import com.libwriting.dao.Word
import com.libwriting.service.SubmitTask
import com.libwriting.ui.CaptureTouchView
import com.libwriting.ui.DrawBaseView
import com.libwriting.ui.PointEx
import com.libwriting.ui.back.ElliBackgroud
import com.libwriting.ui.course.GradeView
import com.libwriting.ui.course.WordView
import com.libwriting.utils.DisplayUtils
import java.io.*

/*
    采集类
    此类用于老师端的笔迹采样
    采样包括：轨迹采样，图像采样，声音采样
    最后把采样的样品提交到后台
 */
class TkSampleFragment : BaseFragment(), ToolBox.OnToolBoxClickListener {
    private val TAG: String = "TkSampleFragment"
    lateinit var binding: TakeSampleFragmentBinding
    private var hasCreated: Boolean = false
    private var selWord: Word? = null
    private var choiceGradeId: String = ""
    private var ttfType: DaoBase.TtfType = DaoBase.TtfType.Kai
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var v = inflater.inflate(R.layout.take_sample_fragment, container, false)
        binding = TakeSampleFragmentBinding.bind(v)
        binding.submitBut.setOnClickListener{onSubmitButClick()}
        binding.videoPlay.setOnClickListener { onVideoPlayClick() }
        binding.toolBox.listener = this
        binding.toolBox.setDefaultPenSize(DrawBaseView.midThick)
        binding.toolBox.showGridButs(false)
        initSampleView()
        hasCreated = true
        addWriteView()
        refreshGrade(null)
        binding.wordPull.setWordViewClickListener(object: OnWordViewClickListener {
            // 点击左边栏目中的文字时触发此事件
            override fun onWordClick(v: WordView) {
                selWord = v.word!!
            }

        })
        return v
    }

    private fun initSampleView() {
        //binding.videoSample.drawDot = false
        binding.videoSample.gridType = DrawBaseView.GridType.TianziType
        binding.videoSample.gridLineDefColor = resources.getColor(R.color.white, null)
        binding.videoSample.gridLineChoiceColor = resources.getColor(R.color.red, null)
        binding.videoSample.backColor = resources.getColor(android.R.color.darker_gray, null)
        binding.videoSample.penColor = resources.getColor(R.color.white, null)
    }

    /*
        从后台获取年级数据，并刷新年级数据
     */
    private fun refreshGrade(id: String?) {
        Grade.queryGrade(object:Grade.GradeResponse {
            override fun onSuccess(results: ArrayList<Grade>?) {
                if (results != null) {
                    System.out.println("result-----"+results.size)
                }
                this@TkSampleFragment.putDataList(results as ArrayList<Any>)
            }
            override fun onFailure(error: String?) {
                PromptMessage.displayToast(context, error)
            }
        })
    }

    override fun onKeiButClick() {
        ttfType = DaoBase.TtfType.Kai
        binding.wordPull.changeTtf(ttfType)
    }
    override fun onXingButClick() {
        ttfType = DaoBase.TtfType.Xing
        binding.wordPull.changeTtf(ttfType)
    }
    override fun onThinButClick(res: Float) {
        getChoiceWrite()?.thick = res
    }
    override fun onClearClick() {
        clearAll()
    }
    override fun onRedoButClick() {
        var v = getChoiceWrite()
        v?.redo()
    }

    override fun onRecordButClick() {
        callRecord(!binding.recordTool.isRecord)
    }

    override fun onTianziGridClick() {
        TODO("Not yet implemented")
    }

    override fun onHTiaoGridClick() {
        TODO("Not yet implemented")
    }

    override fun onVTiaoGridClick() {
        TODO("Not yet implemented")
    }

    private fun clearAll() {
        try {
            getChoiceWrite()?.clearAll()
            binding.videoSample.clearAll()
            binding.recordTool.clearFile()
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun callRecord(start: Boolean) {
        if(start) {
            if(binding.recordTool.startRecord()) {
                binding.recordTool.visibility = View.VISIBLE
                binding.toolBox.modifyRecordButIcon(R.mipmap.microphone_close)
            }
        } else {
            binding.recordTool.stopRecord()
            binding.recordTool.visibility = View.GONE
            binding.toolBox.modifyRecordButIcon(R.mipmap.microphone_open)
        }
    }

    //当点击Video重播按钮时触发此事件
    private fun onVideoPlayClick() {
        var view = getChoiceWrite()
        var pathList = view?.getPathList()
        pathList?.let{binding.videoSample.drawPathPoint(it)}
        if(binding.recordTool.isRecord) {
            callRecord(false)
        }
        var (voiceFile, realFile) = binding.recordTool.convertToWav()
        if(voiceFile != null && realFile != null) {
            binding.videoSample.playVoice(realFile)
        }
    }

    /*
        向后台服务提交采样的数据，包括采样的坐标路径以及图片
     */
    private fun onSubmitButClick() {
        try {
            var view = getChoiceWrite() //得到正在书写的视图
            if(view == null) {
                PromptMessage.displayToast(context, "请选择需要上传的作品")
                return
            }
            if(selWord == null) {
                PromptMessage.displayToast(context, "请选择需要写的字")
                return
            }
            var trackPt = TrackPoint()
            trackPt.userId = (activity?.application as MainApp).teacher.id
            trackPt.wordId = selWord!!.id
            trackPt.wordText = selWord!!.text
            if (trackPt.wordId.isEmpty()) {
                PromptMessage.displayToast(context, "请选择左边需要写的文字")
                return
            }
            trackPt.type = TrackPoint.Type.Word.name
            trackPt.ttf = ttfType.name
            // 笔画粗细
            trackPt.degree = getChoiceWrite()?.thick!!.toInt()
            trackPt.points = getChoiceWrite()?.formatPathToStr().toString()
            val pathList = getChoiceWrite()?.getPathList()
            if (trackPt.points.isEmpty()) {
                PromptMessage.displayToast(context, "请先书写文字")
                return
            }
            trackPt.width = getChoiceWrite()?.width!!
            trackPt.height = getChoiceWrite()?.height!!
            //trackPt.localVideo = view?.createGif(trackPt.makeGifName()).toString()
            //trackPt.gifMake = view?.cloneGif()
            trackPt.putPic(context!!, view?.getBitmap()!!)
            if(binding.recordTool.isRecord) {
                callRecord(false)
            }
            if(binding.recordTool.visibility == View.VISIBLE) {
                var (voiceFile, destFile) = binding.recordTool.convertToWav()
                if (voiceFile != null && destFile != null) {
                    view.startDrawTm?.let {
                        var diffTm = binding.recordTool.startRecordTm - it
                        trackPt.voice = "$diffTm:$voiceFile"
                        trackPt.voiceFileName = voiceFile
                        trackPt.realVoiceFile = destFile
                    }
                }
            }
            // 提交数据
            trackPt.submit(context, object: DaoBase.OnSubmitResponse {
                override fun onSuccess(code: Int, msg: String?, taskId: Int?) {
                    binding.wordPull.setWordStatus(selWord!!.id, true)
                    Toast.makeText(context, "该字已录入成功", Toast.LENGTH_SHORT).show()
                    clearAll()
                }

                override fun onFailure(error: String?, taskId: Int?) {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            }, 1)
            //SubmitTask.get().postTask(trackPt)
        } catch (e: UninitializedPropertyAccessException) {
            PromptMessage.displayToast(context, "内容不能为空")
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    /*
        新增一个书写窗口
        在界面布局Layout中增加一个界面
     */
    private fun addWriteView() {
        var layout = RelativeLayout(context, null)
        var params = LinearLayout.LayoutParams(DisplayUtils.dip2px(context!!, 280f), DisplayUtils.dip2px(context!!, 300f))
        params.gravity = Gravity.CENTER
        layout.layoutParams = params

        var p2 = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        p2.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        var backGroud = ElliBackgroud(context, null)
        backGroud.penColor = resources.getColor(R.color.save_color, null)
        backGroud.backColor = resources.getColor(R.color.black, null)
        backGroud.layoutParams = p2

        var v = CaptureTouchView(context, null)
        var p3 = RelativeLayout.LayoutParams(DisplayUtils.dip2px(context!!, 200f), DisplayUtils.dip2px(context!!, 200f))
        p3.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        v.layoutParams = p3
        v.onChoiceListener = object:  DrawBaseView.OnChoiceListener {
            override fun onChoice(v: DrawBaseView) {
                binding.writeLay.forEach {
                    if(it is RelativeLayout) {
                        for (i in 0 until it.childCount) {
                            if (it[i] is DrawBaseView) {
                                var vv = it[i] as DrawBaseView
                                vv.choice = vv == v
                            }
                        }
                    }
                }
            }
        }
        v.registerDrawObserverFun { t -> binding.videoSample.handleDrawObserver(t) }
        v.choice = true
        v.thick = binding.toolBox.penThickValue
        v.backColor = resources.getColor(R.color.black, null)
        v.penColor = resources.getColor(R.color.white, null)
        v.choiceColor = v.penColor
        v.cols = 2
        v.rows = 2
        v.gridType = DrawBaseView.GridType.TianziType
        v.gridLineChoiceColor = resources.getColor(R.color.red, null)
        v.gridLineDefColor = resources.getColor(R.color.white, null)
        v.enableFingerDraw = false

        layout.addView(backGroud)
        layout.addView(v)
        binding.writeLay.addView(layout)
        //滚动条调整到合适的位置
//        binding.scrollWrite.post {
//            binding.scrollWrite.scrollX = binding.writeLay.left
//            binding.scrollWrite.scrollY = binding.writeLay.top
//            binding.scrollWrite.fullScroll(ScrollView.FOCUS_RIGHT)
//        }
    }
    /*
        移除一个书写窗口
        从Layout布局中删除一个WriteView窗口
     */
    private fun removeWriteView() {
        binding.writeLay.forEach {
            if(it is RelativeLayout) {
                for(i in 0 until it.childCount) {
                    if(it[i] is DrawBaseView) {
                        var v = it[i] as DrawBaseView
                        if (v.choice) {
                            v.clearAll()
                            binding.writeLay.removeView(it)
//                            binding.scrollWrite.post { binding.scrollWrite.fullScroll(ScrollView.FOCUS_RIGHT) }
                            return@forEach
                        }
                    }
                }
            }
        }
    }
    /*
        返回一个被选择的writeView对象
     */
    private fun getChoiceWrite() : CaptureTouchView?{
        var view: CaptureTouchView? = null
        binding.writeLay.forEach {
            if(it is RelativeLayout) {
                for (i in 0 until it.childCount) {
                    if (it[i] is DrawBaseView) {
                        var v = it[i] as CaptureTouchView
                        if(v.choice) {
                            view = v
                            return@forEach
                        }
                    }
                }
            }
        }
        return view
    }


    /*
        根据从后台查询的年级数据
        初始化年级视图UI界面
     */
    private fun initGradeView(obj: Grade?) {
        if(obj == null) {
            return
        }
        var v = GradeView(context, null)
        var params = LinearLayout.LayoutParams(300, LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER
        params.setMargins(1, 1, 1, 1)
        v.layoutParams = params
        v.setGrade(obj)
        if(obj.level == 1) {
            choiceGradeId = obj.id
            binding.wordPull.setGradeId(choiceGradeId)
        }
        v.setOnClickListener { v -> onGradeClick(v as GradeView)}
        binding.gradeLayout.addView(v)
    }

    /*
        点击年级按钮后，需要向后台查询年级对应的文字数据
     */
    private fun onGradeClick(v: GradeView) {
        for(i in 0 until binding.gradeLayout.childCount) {
            var vv = binding.gradeLayout[i] as GradeView
            vv.choice(vv == v)
        }
        choiceGradeId = v.getGrade().id
        binding.wordPull.setGradeId(choiceGradeId)
    }

    override fun onLazyLoad(obj: Any) {
        if ( obj is Grade) {
            initGradeView(obj)
        } else if(obj is Word) {
            //handleInitWord(obj)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

