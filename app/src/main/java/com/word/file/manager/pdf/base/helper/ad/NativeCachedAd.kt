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
        val view = NativeAdView(activity).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        val root = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 16, 20, 16)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        val media = MediaView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 320)
            isVisible = style == NativeAdStyle.Media
            mediaContent = ad.mediaContent
        }
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 12, 0, 0)
        }
        val icon = ImageView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(72, 72)
            setImageDrawable(ad.icon?.drawable)
        }
        val textColumn = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 0, 16, 0)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val headline = TextView(activity).apply {
            text = ad.headline.orEmpty()
            setTextColor(Color.BLACK)
            textSize = 14f
            maxLines = 1
        }
        val body = TextView(activity).apply {
            text = ad.body.orEmpty()
            setTextColor(Color.DKGRAY)
            textSize = 12f
            maxLines = 1
        }
        val action = Button(activity).apply {
            text = ad.callToAction ?: "View"
            isAllCaps = false
        }
        textColumn.addView(headline)
        textColumn.addView(body)
        row.addView(icon)
        row.addView(textColumn)
        root.addView(media)
        root.addView(row)
        root.addView(action, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        view.addView(root)
        view.mediaView = media
        view.iconView = icon
        view.headlineView = headline
        view.bodyView = body
        view.callToActionView = action
        view.setNativeAd(ad)
        return view
    }
}
