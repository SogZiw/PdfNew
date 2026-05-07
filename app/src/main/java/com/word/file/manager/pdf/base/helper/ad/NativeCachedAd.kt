package com.word.file.manager.pdf.base.helper.ad

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.BaseActivity

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

    }
}
