package com.word.file.manager.pdf.base.helper.ad.nativead

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.LocalPrefs
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.ad.cache.CachedAd
import com.word.file.manager.pdf.base.helper.ad.model.AdSlot
import com.word.file.manager.pdf.base.helper.ad.model.AdUnitConfig
import com.word.file.manager.pdf.base.helper.ad.model.NativeAdStyle
import com.word.file.manager.pdf.base.helper.ad.util.NativeAdPreviewChecker
import com.word.file.manager.pdf.base.helper.remote.RemoteLogicConfig
import com.word.file.manager.pdf.databinding.LayoutNativeAdCompactBinding
import com.word.file.manager.pdf.databinding.LayoutNativeAdFullBinding
import com.word.file.manager.pdf.databinding.LayoutNativeAdFullShineBinding
import com.word.file.manager.pdf.ext.startBackgroundAnimation

abstract class CachedNativeAd(
    config: AdUnitConfig,
    slot: AdSlot,
) : CachedAd(config, slot) {
    abstract fun render(activity: BaseActivity<*>, container: ViewGroup, style: NativeAdStyle)
}

class AdmobNativeCachedAd(
    config: AdUnitConfig,
    slot: AdSlot,
) : CachedNativeAd(config, slot) {

    private var loadedAd: NativeAd? = null

    override fun preload(done: (Boolean) -> Unit) {
        logState("load start")
        AdLoader.Builder(app, config.placementId)
            .forNativeAd { ad ->
                loadedAd = ad
                if (LocalPrefs.isPreviewUser) {
                    val isTestUser = NativeAdPreviewChecker.isPreviewUser(ad.headline)
                    LocalPrefs.isPreviewUser = isTestUser
                    if (isTestUser) EventCenter.logEvent("ad_test")
                }
                ad.setOnPaidEventListener { logPaidValue(it, ad.responseInfo?.loadedAdapterResponseInfo) }
                logState("load success")
                done(true)
            }
            .withAdListener(
                object : AdListener() {
                    override fun onAdClicked() {
                        logClick()
                    }

                    override fun onAdImpression() {
                        logImpression()
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        logState("load failed ${error.code}: ${error.message}")
                        done(false)
                    }
                },
            )
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build(),
            )
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    override fun render(activity: BaseActivity<*>, container: ViewGroup, style: NativeAdStyle) {
        val ad = loadedAd ?: return
        val adView = buildNativeCard(activity, ad, style)
        container.isVisible = true
        container.removeAllViews()
        container.addView(adView)
    }

    override fun release() {
        runCatching { loadedAd?.destroy() }
        loadedAd = null
    }

    private fun buildNativeCard(activity: BaseActivity<*>, ad: NativeAd, style: NativeAdStyle): NativeAdView {
        return when (style) {
            NativeAdStyle.COMMON_MEDIA -> {
                LayoutNativeAdFullBinding.inflate(LayoutInflater.from(activity)).apply {
                    bindAdContent(ad)
                }.root
            }

            NativeAdStyle.NO_ACTION_MEDIA -> {
                LayoutNativeAdCompactBinding.inflate(LayoutInflater.from(activity)).apply {
                    bindAdContent(ad)
                }.root
            }

            NativeAdStyle.ANIM_MEDIA -> {
                LayoutNativeAdFullShineBinding.inflate(LayoutInflater.from(activity)).apply {
                    bindAdContent(ad)
                }.root
            }
        }
    }

    private fun LayoutNativeAdFullShineBinding.bindAdContent(ad: NativeAd) {
        root.apply {
            iconView = adAppIcon.apply { setImageDrawable(ad.icon?.drawable) }
            headlineView = adTitleText.apply { text = ad.headline.orEmpty() }
            bodyView = adDescText.apply { text = ad.body.orEmpty() }
            callToActionView = adCtaButton.apply { text = ad.callToAction.orEmpty() }
            if (RemoteLogicConfig.fetchPromotionConfig().uiThemeSync && UserBlockHelper.canShowExtra()) {
                adCtaButton.setBackgroundResource(R.drawable.btn_primary_shine_r10)
                adCtaButton.startBackgroundAnimation()
            } else {
                adCtaButton.setBackgroundResource(R.drawable.btn_primary_r10_ad)
            }
            mediaView = adMediaArea.apply {
                mediaContent = ad.mediaContent
                setImageScaleType(ImageView.ScaleType.CENTER_CROP)
            }
            setNativeAd(ad)
        }
    }

    private fun LayoutNativeAdFullBinding.bindAdContent(ad: NativeAd) {
        root.apply {
            iconView = adAppIcon.apply { setImageDrawable(ad.icon?.drawable) }
            headlineView = adTitleText.apply { text = ad.headline.orEmpty() }
            bodyView = adDescText.apply { text = ad.body.orEmpty() }
            callToActionView = adCtaButton.apply { text = ad.callToAction.orEmpty() }
            adCtaButton.setBackgroundResource(if (RemoteLogicConfig.fetchPromotionConfig().uiThemeSync && UserBlockHelper.canShowExtra()) R.drawable.btn_primary_r10 else R.drawable.btn_primary_r10_ad)
            mediaView = adMediaArea.apply {
                mediaContent = ad.mediaContent
                setImageScaleType(ImageView.ScaleType.CENTER_CROP)
            }
            setNativeAd(ad)
        }
    }

    private fun LayoutNativeAdCompactBinding.bindAdContent(ad: NativeAd) {
        root.apply {
            iconView = adAppIcon.apply { setImageDrawable(ad.icon?.drawable) }
            headlineView = adTitleText.apply { text = ad.headline.orEmpty() }
            bodyView = adDescText.apply { text = ad.body.orEmpty() }
            callToActionView = adCtaButton.apply { text = ad.callToAction.orEmpty() }
            mediaView = adMediaArea.apply {
                mediaContent = ad.mediaContent
                setImageScaleType(ImageView.ScaleType.CENTER_CROP)
            }
            setNativeAd(ad)
        }
    }
}
