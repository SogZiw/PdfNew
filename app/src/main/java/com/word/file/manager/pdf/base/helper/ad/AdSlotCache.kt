package com.word.file.manager.pdf.base.helper.ad

import android.view.ViewGroup
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.utils.showLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

    fun hasCachedAd(): Boolean {
        removeExpiredAds()
        return cachedAds.isNotEmpty()
    }

    fun preload() {
        adPipelineScope.launch { preloadIfNeeded() }
    }

    fun showFullScreen(
        activity: BaseActivity<*>,
        allowed: () -> Boolean = { true },
        closed: () -> Unit = {},
        shown: () -> Unit = {},
    ) {
        adPipelineScope.launch {
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
            cachedAd.show(activity, closed, shown)
            preloadIfNeeded()
        }
    }

    fun renderNative(
        activity: BaseActivity<*>,
        host: ViewGroup,
        style: NativeAdStyle,
        allowed: () -> Boolean = { true },
    ) {
        if (!allowed()) return
        waitForNativeAd {
            adPipelineScope.launch {
                val cachedAd = takeCachedAd<CachedNativeAd>() ?: return@launch
                cachedAd.render(activity, host, style)
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

    private enum class LoadState {
        Idle,
        Loading,
    }

    private companion object {
        const val ADMOB = "admob"
    }
}
