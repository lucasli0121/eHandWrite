package com.handwrite.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.handwrite.databinding.DescActivityBinding
import com.handwrite.utils.PromptMessage

class DescActivity: AppCompatActivity() {
    private lateinit var binding: DescActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DescActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.okBut.setOnClickListener {
            if(binding.desc.text.isEmpty()) {
                PromptMessage.displayToast(this, "内容不能为空")
                return@setOnClickListener
            }
            intent.putExtra("desc", binding.desc.text.toString())
            setResult(RESULT_OK, intent)
            finish()
        }
        binding.cancelBut.setOnClickListener {
            finish()
        }
    }
}