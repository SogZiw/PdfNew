package com.word.file.manager.pdf.modules.guide

import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.databinding.ActivityGuideFirstBinding

class GuideFirstActivity : BaseActivity<ActivityGuideFirstBinding>() {

    override fun setViewBinding() = ActivityGuideFirstBinding.inflate(layoutInflater)

    override fun initView() {
        binding.toolbar.toolbarTitle.text = getString(R.string.guide_first_title)
        binding.toolbar.actionBack.setOnClickListener { }
        binding.guideFirstPdfRetryButton.setOnClickListener { }
        binding.guideFirstFeatureExploreButton.setOnClickListener { }
        binding.guideFirstKeepButton.setOnClickListener { }
        binding.guideFirstContinueUninstallButton.setOnClickListener { }
    }

    override fun onClickBack() = Unit
}
