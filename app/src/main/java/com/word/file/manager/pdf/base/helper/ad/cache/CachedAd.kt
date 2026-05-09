package com.word.file.manager.pdf.base.helper.ad.cache

import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdapterResponseInfo
import com.word.file.manager.pdf.base.helper.ad.model.AdSlot
import com.word.file.manager.pdf.base.helper.ad.model.AdUnitConfig
import com.word.file.manager.pdf.base.helper.ad.util.AdRevenueUtils
import com.word.file.manager.pdf.base.utils.showLog

abstract class CachedAd(
    protected val config: AdUnitConfig,
    protected val slot: AdSlot,
) {
    private val loadedAt = System.currentTimeMillis()

    abstract fun preload(done: (Boolean) -> Unit)

    open fun release() = Unit

    fun isExpired(): Boolean {
        return config.maxAgeMillis > 0L && (System.currentTimeMillis() - loadedAt >= config.maxAgeMillis)
    }

    protected fun logState(message: String) {
        "${slot.jsonKey}/${config.type.reportName}/${config.placementId}: $message".showLog("AdCenter")
    }

    protected fun logPaidValue(adValue: AdValue, adapter: AdapterResponseInfo?) {
        logState("paid value ${adValue.valueMicros} ${adValue.currencyCode} from ${adapter?.adSourceName.orEmpty()}")
        AdRevenueUtils.logPaidValue(slot, config, adValue, adapter)
    }

    protected fun logClick() {
        logState("click")
    }

    protected fun logImpression() {
        logState("impression")
    }
}
