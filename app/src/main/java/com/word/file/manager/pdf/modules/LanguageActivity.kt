package com.word.file.manager.pdf.modules

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.word.file.manager.pdf.AD_POS_ID
import com.word.file.manager.pdf.APP_AD_IMPRESSION
import com.word.file.manager.pdf.EXTRA_DOCUMENT_ACTION_TYPE
import com.word.file.manager.pdf.EXTRA_FROM_SET
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.data.DocumentActionType
import com.word.file.manager.pdf.base.data.DocumentOpenType
import com.word.file.manager.pdf.base.data.LanguageCatalog
import com.word.file.manager.pdf.base.data.LanguageItem
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.LocalPrefs
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.base.helper.ad.model.NativeAdStyle
import com.word.file.manager.pdf.databinding.ActivityLanguageBinding
import com.word.file.manager.pdf.databinding.ItemLanguageBinding
import java.util.Locale

class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {

    private lateinit var languageAdapter: LanguageAdapter
    private val fromSet by lazy { intent?.getBooleanExtra(EXTRA_FROM_SET, true) ?: true }

    override fun setViewBinding(): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        binding.toolbar.actionBack.setOnClickListener { onClickBack() }
        binding.toolbar.toolbarTitle.text = getString(R.string.language)
        binding.toolbar.actionBack.isInvisible = fromSet.not()
        setupLanguageList()
        binding.btnApply.setOnClickListener { applySelectedLanguage() }
        AdCenter.scanInterstitial.preload()
        AdCenter.backInterstitial.preload()
        AdCenter.mainNative.renderNative(
            activity, binding.exContainer, NativeAdStyle.Media, eventName = "ad_new_langua_nat",
            allowed = { UserBlockHelper.canShowExtra() })
    }

    private fun setupLanguageList() {
        val selectedCode = LocalPrefs.defaultLanguageCode.ifBlank { Locale.getDefault().language }
        val languages = LanguageCatalog.supportedLanguages
            .sortedByDescending { it.languageCode == selectedCode }
            .toMutableList()
        val selectedIndex = languages.indexOfFirst { it.languageCode == selectedCode }.takeIf { it >= 0 } ?: 0
        languageAdapter = LanguageAdapter(languages).apply {
            selectedPosition = selectedIndex
        }
        binding.recyclerView.itemAnimator = null
        binding.recyclerView.adapter = languageAdapter
    }

    private fun applySelectedLanguage() {
        LocalPrefs.defaultLanguageCode = languageAdapter.selectedLanguage.languageCode
        if (fromSet) {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
            finish()
        } else {
            LocalPrefs.hasSeenIntroduce = true
            EventCenter.logEvent(APP_AD_IMPRESSION, mapOf(AD_POS_ID to "ad_new_langua_int"))
            AdCenter.backInterstitial.showFullScreen(activity, eventName = "ad_new_langua_int", allowed = {
                UserBlockHelper.canShowExtra()
            }, closed = {
                startActivity(Intent(activity, IntroduceActivity::class.java).apply {
                    putExtra(EXTRA_DOCUMENT_ACTION_TYPE, readLaunchActionType())
                })
                finish()
            })
        }
    }

    private fun readLaunchActionType(): DocumentActionType {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(EXTRA_DOCUMENT_ACTION_TYPE, DocumentActionType::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_DOCUMENT_ACTION_TYPE)
        } ?: DocumentOpenType
    }

    private class LanguageAdapter(
        private val items: List<LanguageItem>,
    ) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

        var selectedPosition: Int = 0
            set(value) {
                field = value.coerceIn(items.indices)
            }

        val selectedLanguage: LanguageItem
            get() = items[selectedPosition]

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
            return LanguageViewHolder(
                ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            )
        }

        override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
            val item = items[position]
            val selected = position == selectedPosition
            holder.binding.textLanguageName.text = item.displayName
            holder.binding.root.isSelected = selected
            holder.binding.imageCheck.setImageResource(
                if (selected) R.drawable.ic_language_selected else R.drawable.ic_language_unselected,
            )
            holder.itemView.setOnClickListener {
                updateSelection(holder.bindingAdapterPosition)
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun updateSelection(position: Int) {
            if (position == RecyclerView.NO_POSITION || position == selectedPosition) return
            selectedPosition = position
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = items.size

        class LanguageViewHolder(
            val binding: ItemLanguageBinding,
        ) : RecyclerView.ViewHolder(binding.root)
    }
}
