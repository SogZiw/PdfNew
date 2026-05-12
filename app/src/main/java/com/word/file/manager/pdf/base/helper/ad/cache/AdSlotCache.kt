package com.word.file.manager.pdf.base.helper.ad.cache

import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.word.file.manager.pdf.ADMOB
import com.word.file.manager.pdf.AD_POS_ID
import com.word.file.manager.pdf.APP_AD_CHANCE
import com.word.file.manager.pdf.APP_AD_IMPRESSION
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.base.helper.ad.fullscreen.AdmobFullScreenCachedAd
import com.word.file.manager.pdf.base.helper.ad.fullscreen.CachedFullScreenAd
import com.word.file.manager.pdf.base.helper.ad.model.AdSlot
import com.word.file.manager.pdf.base.helper.ad.model.AdType
import com.word.file.manager.pdf.base.helper.ad.model.AdUnitConfig
import com.word.file.manager.pdf.base.helper.ad.model.LoadState
import com.word.file.manager.pdf.base.helper.ad.model.NativeAdStyle
import com.word.file.manager.pdf.base.helper.ad.nativead.AdmobNativeCachedAd
import com.word.file.manager.pdf.base.helper.ad.nativead.CachedNativeAd
import com.word.file.manager.pdf.base.helper.remote.RemoteLogicConfig
import com.word.file.manager.pdf.base.utils.showLog
import com.word.file.manager.pdf.databinding.DialogPdfWorkingBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val adPipelineScope by lazy {
    CoroutineScope(
        Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { _, error ->
            error.message.orEmpty().showLog("AdCenter")
        },
    )
}

class AdSlotCache(private val slot: AdSlot) {

    private val configs = mutableListOf<AdUnitConfig>()
    private val cachedAds = ArrayDeque<CachedAd>()

    private var loadState: LoadState = LoadState.Idle
    private var waitingCallback: (Boolean) -> Unit = {}

    fun updateConfigs(newConfigs: List<AdUnitConfig>) {
        configs.clear()
        configs.addAll(newConfigs)
    }

    fun getAdUnitConf() = configs.firstOrNull()

    fun hasCachedAd(): Boolean {
        removeExpiredAds()
        return cachedAds.isNotEmpty()
    }

    fun preload() {
        adPipelineScope.launch { preloadIfNeeded() }
    }

    fun showFullScreen(
        activity: BaseActivity<*>,
        eventName: String = slot.jsonKey,
        allowed: () -> Boolean = { true },
        closed: () -> Unit = {},
        shown: () -> Unit = {},
        clicked: () -> Unit = {},
    ) {
        adPipelineScope.launch {
            if ((System.currentTimeMillis() - AdCenter.lastFullAdShowTime) < RemoteLogicConfig.fetchPromotionConfig().fullAdLimit) {
                closed()
                return@launch
            }
            if (!allowed()) {
                closed()
                return@launch
            }
            val cachedAd = takeCachedAd<CachedFullScreenAd>()
            if (cachedAd == null) {
                closed()
                preloadIfNeeded()
                return@launch
            }
            if (cachedAd.shouldPrepareBeforeShow()) {
                val dialog = activity.showAdPreparingDialog()
                delay(850L)
                dialog.dismiss()
            }
            cachedAd.show(activity = activity, closed = closed, shown = shown, clicked = clicked)
            EventCenter.logEvent(APP_AD_IMPRESSION, mapOf(AD_POS_ID to eventName))
            preloadIfNeeded()
        }
    }

    fun renderNative(
        activity: BaseActivity<*>,
        host: ViewGroup,
        style: NativeAdStyle,
        eventName: String = slot.jsonKey,
        allowed: () -> Boolean = { true },
        shown: () -> Unit = {},
    ) {
        if (!allowed()) return
        EventCenter.logEvent(APP_AD_CHANCE, mapOf(AD_POS_ID to eventName))
        waitForNativeAd {
            adPipelineScope.launch {
                while (!activity.fetchResumeState()) delay(250L)
                val cachedAd = takeCachedAd<CachedNativeAd>() ?: return@launch
                activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
                    override fun onDestroy(owner: LifecycleOwner) = cachedAd.release()
                })
                cachedAd.render(activity, host, style)
                shown()
                EventCenter.logEvent(APP_AD_IMPRESSION, mapOf(AD_POS_ID to eventName))
                preloadIfNeeded()
            }
        }
    }

    private fun waitForNativeAd(callback: (Boolean) -> Unit) {
        waitingCallback = callback
        if (configs.isEmpty()) {
            waitingCallback(false)
            return
        }
        if (cachedAds.isEmpty()) preload() else waitingCallback(true)
    }

    private fun preloadIfNeeded() {
        if (configs.isEmpty()) return
        removeExpiredAds()
        if (cachedAds.isNotEmpty() || loadState == LoadState.Loading) return
        loadState = LoadState.Loading
        loadConfigAt(0)
    }

    private fun loadConfigAt(index: Int) {
        val config = configs.getOrNull(index)
        if (config == null) {
            finish(false)
            return
        }
        val cachedAd = config.createCachedAd()
        if (cachedAd == null) {
            loadConfigAt(index + 1)
            return
        }
        cachedAd.preload { loaded ->
            if (loaded) {
                cachedAds.addLast(cachedAd)
                finish(true)
            } else {
                loadConfigAt(index + 1)
            }
        }
    }

    private inline fun <reified T : CachedAd> takeCachedAd(): T? {
        removeExpiredAds()
        val cachedAd = cachedAds.removeFirstOrNull() ?: return null
        return cachedAd as? T
    }

    private fun finish(success: Boolean) {
        loadState = LoadState.Idle
        waitingCallback(success)
        waitingCallback = {}
    }

    private fun removeExpiredAds() {
        cachedAds.removeAll { it.isExpired() }
    }

    private fun AdUnitConfig.createCachedAd(): CachedAd? {
        if (provider != ADMOB) return null
        return when (type) {
            AdType.AppOpen,
            AdType.Interstitial -> AdmobFullScreenCachedAd(this, slot)

            AdType.Native -> AdmobNativeCachedAd(this, slot)
            else -> null
        }
    }

    private fun BaseActivity<*>.showAdPreparingDialog() = MaterialAlertDialogBuilder(this)
        .setView(
            DialogPdfWorkingBinding.inflate(layoutInflater).apply {
                textProgress.text = getString(R.string.preparing_ad)
            }.root,
        )
        .setCancelable(false)
        .create()
        .also { it.show() }
}
