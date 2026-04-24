package com.word.file.manager.pdf

import android.view.LayoutInflater
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val applyRootInsets: Boolean = true

    override fun setViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        // init view here
    }
}
