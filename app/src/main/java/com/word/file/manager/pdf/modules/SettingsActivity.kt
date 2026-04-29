package com.word.file.manager.pdf.modules

import android.content.Intent
import android.view.LayoutInflater
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.utils.showMessageToast
import com.word.file.manager.pdf.databinding.ActivitySettingsBinding

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {

    override fun setViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        binding.toolbar.actionBack.setOnClickListener { onClickBack() }
        binding.toolbar.toolbarTitle.text = getString(R.string.settings)
        binding.btnLanguage.setOnClickListener { openLanguageSettings() }
        binding.btnPrivacy.setOnClickListener { openPrivacyPolicy() }
    }

    private fun openLanguageSettings() {
        startActivity(Intent(this, LanguageActivity::class.java))
    }

    private fun openPrivacyPolicy() {
        runCatching {
            CustomTabsIntent.Builder().build().launchUrl(this, PRIVACY_POLICY_URL.toUri())
        }.onFailure {
            openPrivacyPolicyByBrowser()
        }
    }

    private fun openPrivacyPolicyByBrowser() {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, PRIVACY_POLICY_URL.toUri()))
        }.onFailure {
            showMessageToast(getString(R.string.common_error_message))
        }
    }

    private companion object {
        const val PRIVACY_POLICY_URL = "https://www.google.com"
    }
}
