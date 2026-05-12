package com.word.file.manager.pdf.base.helper.ad.fullscreen

import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.helper.ad.cache.CachedAd
import com.word.file.manager.pdf.base.helper.ad.model.AdSlot
import com.word.file.manager.pdf.base.helper.ad.model.AdType
import com.word.file.manager.pdf.base.helper.ad.model.AdUnitConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class CachedFullScreenAd(
    config: AdUnitConfig,
    slot: AdSlot,
) : CachedAd(config, slot) {

    abstract fun show(activity: BaseActivity<*>, closed: () -> Unit, shown: () -> Unit = {}, clicked: () -> Unit = {})

    fun shouldPrepareBeforeShow(): Boolean {
        return config.type == AdType.Interstitial
    }

    protected fun resumeThen(activity: BaseActivity<*>, block: () -> Unit) {
        activity.lifecycleScope.launch(Dispatchers.Main) {
            while (!activity.fetchResumeState()) delay(250L)
            block()
        }
    }
}

class AdmobFullScreenCachedAd(
    config: AdUnitConfig,
    slot: AdSlot,
) : CachedFullScreenAd(config, slot) {

    private var loadedAd: Any? = null

    override fun preload(done: (Boolean) -> Unit) {
        logState("load start")
        when (config.type) {
            AdType.AppOpen -> {
                AppOpenAd.load(
                    app,
                    config.placementId,
                    AdRequest.Builder().build(),
                    object : AppOpenAd.AppOpenAdLoadCallback() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            logState("load failed ${error.code}: ${error.message}")
                            done(false)
                        }

                        override fun onAdLoaded(ad: AppOpenAd) {
                            loadedAd = ad
                            ad.setOnPaidEventListener { logPaidValue(it, ad.responseInfo.loadedAdapterResponseInfo) }
                            logState("load success")
                            done(true)
                        }
                    },
                )
            }

            AdType.Interstitial -> {
                InterstitialAd.load(
                    app,
                    config.placementId,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            logState("load failed ${error.code}: ${error.message}")
                            done(false)
                        }

                        override fun onAdLoaded(ad: InterstitialAd) {
                            loadedAd = ad
                            ad.setOnPaidEventListener { logPaidValue(it, ad.responseInfo.loadedAdapterResponseInfo) }
                            logState("load success")
                            done(true)
                        }
                    },
                )
            }

            else -> {
                logState("ignored non screen type")
                done(false)
            }
        }
    }

    override fun show(activity: BaseActivity<*>, closed: () -> Unit, shown: () -> Unit, clicked: () -> Unit) {
        val callback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                resumeThen(activity, closed)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                logState("show failed ${error.code}: ${error.message}")
                resumeThen(activity, closed)
            }

            override fun onAdShowedFullScreenContent() {
                logImpression()
                shown()
            }

            override fun onAdClicked() {
                logClick()
                clicked()
            }
        }
        when (val ad = loadedAd) {
            is AppOpenAd -> {
                ad.fullScreenContentCallback = callback
                ad.setImmersiveMode(true)
                ad.show(activity)
            }

            is InterstitialAd -> {
                ad.fullScreenContentCallback = callback
                ad.setImmersiveMode(true)
                ad.show(activity)
            }

            else -> resumeThen(activity, closed)
        }
    }
}
