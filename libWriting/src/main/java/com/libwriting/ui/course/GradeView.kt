package com.libwriting.ui.course

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.libwriting.dao.Grade
import com.write.libwriting.R

/*
    年级视图，用于在UI界面显示年级信息的视图
    采用横排的方式显示，可以显示多个年级
 */
class GradeView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private var bm : Bitmap? = null
    private lateinit var grade: Grade
    private var imgView: ImageView
    private var textView: TextView
    init {
        bm = BitmapFactory.decodeResource(resources, R.mipmap.grade)
        orientation = VERTICAL
        var params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        params.gravity = Gravity.CENTER
        layoutParams = params
        imgView = ImageView(context, null)
        params = LayoutParams(LayoutParams.MATCH_PARENT, 0)
        params.weight = 0.75f
        params.gravity = Gravity.CENTER
        imgView.layoutParams = params
        imgView.scaleType = ImageView.ScaleType.CENTER_CROP
        imgView.adjustViewBounds = true
        imgView.setImageBitmap(bm)
        addView(imgView)
        textView = TextView(context, null)
        params = LayoutParams(LayoutParams.MATCH_PARENT, 0)
        params.weight = 0.25f
        params.gravity = Gravity.TOP
        textView.layoutParams = params
        textView.setTextAppearance(R.style.PromptTextStyle2)
        textView.textAlignment = TEXT_ALIGNMENT_CENTER
        addView(textView)
    }
    public fun getGrade() : Grade {
        return grade
    }
    public fun setGrade(g: Grade) {
        grade = g
        if(g.level == 1) {
            background = resources.getDrawable(R.drawable.imageview_shape, null)
        } else {
            background = resources.getDrawable(R.drawable.imageview_unsel_shape, null)
        }
        textView.text = g.name
    }
    public fun choice(c: Boolean) {
        background =  if (c) resources.getDrawable(R.drawable.imageview_shape, null) else resources.getDrawable(
            R.drawable.imageview_unsel_shape, null)
    }
}