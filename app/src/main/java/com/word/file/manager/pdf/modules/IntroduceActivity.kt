package com.word.file.manager.pdf.modules

import android.content.Intent
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.word.file.manager.pdf.AD_POS_ID
import com.word.file.manager.pdf.APP_AD_IMPRESSION
import com.word.file.manager.pdf.EXTRA_DOCUMENT_ACTION_TYPE
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.data.DocumentActionType
import com.word.file.manager.pdf.base.data.DocumentOpenType
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.LocalPrefs
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.base.helper.ad.model.NativeAdStyle
import com.word.file.manager.pdf.base.helper.remote.RemoteLogicConfig
import com.word.file.manager.pdf.databinding.ActivityIntroduceBinding
import kotlin.math.roundToInt

class IntroduceActivity : BaseActivity<ActivityIntroduceBinding>() {

    private val introPages = listOf(
        IntroPage(R.drawable.ic_intro_1, R.string.introduce_title_1, R.string.introduce_desc_1),
        IntroPage(R.drawable.ic_intro_2, R.string.introduce_title_2, R.string.introduce_desc_2),
        IntroPage(R.drawable.ic_intro_3, R.string.introduce_title_3, R.string.introduce_desc_3),
    )

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            bindIntroText(position)
            refreshIndicators(position)
        }
    }

    override fun setViewBinding() = ActivityIntroduceBinding.inflate(layoutInflater)

    override fun initView() {
        binding.introducePager.adapter = IntroImageAdapter(introPages.map { it.imageRes })
        binding.introducePager.registerOnPageChangeCallback(pageChangeCallback)
        binding.introduceSkip.setOnClickListener { goMainPage() }
        binding.introduceNextButton.setOnClickListener { goMainPage() }
        bindIntroText(position = 0)
        refreshIndicators(position = 0)
        AdCenter.backInterstitial.preload()
        AdCenter.scanNative.renderNative(
            activity, binding.exContainer, NativeAdStyle.COMMON_MEDIA, eventName = "ad_new_intro_nat",
            allowed = { RemoteLogicConfig.fetchPromotionConfig().initIntroNat && UserBlockHelper.canShowExtra() })
    }

    private fun goMainPage() {
        LocalPrefs.hasSeenIntroduce = true
        EventCenter.logEvent(APP_AD_IMPRESSION, mapOf(AD_POS_ID to "ad_new_intro_int"))
        AdCenter.backInterstitial.showFullScreen(activity, eventName = "ad_new_intro_int", allowed = {
            RemoteLogicConfig.fetchPromotionConfig().initIntroInt && UserBlockHelper.canShowExtra()
        }, closed = {
            startActivity(Intent(activity, MainActivity::class.java).apply {
                putExtra(EXTRA_DOCUMENT_ACTION_TYPE, readLaunchActionType())
            })
            finish()
        })
    }

    private fun readLaunchActionType(): DocumentActionType {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(EXTRA_DOCUMENT_ACTION_TYPE, DocumentActionType::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_DOCUMENT_ACTION_TYPE)
        } ?: DocumentOpenType
    }

    private fun bindIntroText(position: Int) {
        val page = introPages.getOrNull(position) ?: return
        binding.introduceTitle.setText(page.titleRes)
        binding.introduceDesc.setText(page.descRes)
        binding.introduceNextButton.setText(if (position == introPages.lastIndex) R.string.start else R.string.next)
    }

    private fun refreshIndicators(position: Int) {
        listOf(
            binding.introduceIndicator1,
            binding.introduceIndicator2,
            binding.introduceIndicator3,
        ).forEachIndexed { index, view ->
            val selected = index == position
            view.setBackgroundResource(
                if (selected) R.drawable.shape_intro_indicator_active else R.drawable.shape_intro_indicator_inactive
            )
            view.layoutParams = view.layoutParams.apply {
                width = if (selected) 16.dp() else 5.dp()
                height = 5.dp()
            }
        }
    }

    override fun onUserBack() = Unit

    override fun onDestroy() {
        binding.introducePager.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onDestroy()
    }

    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).roundToInt()
    }

    private data class IntroPage(
        val imageRes: Int,
        val titleRes: Int,
        val descRes: Int,
    )

    private class IntroImageAdapter(
        private val imageResList: List<Int>,
    ) : RecyclerView.Adapter<IntroImageAdapter.ImageHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
            val imageView = AppCompatImageView(parent.context).apply {
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
            return ImageHolder(imageView)
        }

        override fun onBindViewHolder(holder: ImageHolder, position: Int) {
            holder.imageView.setImageResource(imageResList[position])
        }

        override fun getItemCount() = imageResList.size

        class ImageHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: AppCompatImageView = view as AppCompatImageView
        }
    }
}
