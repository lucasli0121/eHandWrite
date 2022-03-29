package com.handwrite.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.handwrite.R
import com.handwrite.databinding.PoetryTypeBinding

class PoetryType(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs)  {
    private lateinit var binding: PoetryTypeBinding
    init {
        var inflater = LayoutInflater.from(context)
        var v = inflater.inflate(R.layout.poetry_type, this, true)
        binding = PoetryTypeBinding.bind(v)
        binding.type1.setOnClickListener { v -> handleViewClick(v) }
    }

    private fun handleViewClick(v: View) {
        Log.v("type", "client this view")
    }
}