package com.word.file.manager.pdf.base.helper.ad.cache

import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdapterResponseInfo
import com.google.firebase.analytics.FirebaseAnalytics
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.LocalPrefs
import com.word.file.manager.pdf.base.helper.ad.model.AdSlot
import com.word.file.manager.pdf.base.helper.ad.model.AdUnitConfig
import com.word.file.manager.pdf.base.helper.net.NetworkCenter
import com.word.file.manager.pdf.base.utils.showLog
import org.json.JSONObject

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
        val revenue: Double = adValue.valueMicros / 1000000.toDouble()
        EventCenter.logEvent("ad_impression_revenue", mapOf(FirebaseAnalytics.Param.VALUE to revenue, FirebaseAnalytics.Param.CURRENCY to "USD"), true)
        runCatching { EventCenter.facebookPurchase(revenue) }
        NetworkCenter.adImpression(JSONObject().apply {
            put("tore", adValue.valueMicros)
            put("languish", adValue.currencyCode)
            put("hitch", adapter?.adSourceName ?: "")
            put("crept", config.provider)
            put("remedy", config.placementId)
            put("straw", slot.jsonKey)
            put("necrosis", config.type.reportName)
        })
        taichi001(revenue)
        Adjust.trackAdRevenue(AdjustAdRevenue("admob_sdk").apply {
            setRevenue(revenue, adValue.currencyCode)
            adRevenueNetwork = adapter?.adSourceName ?: ""
        })
    }

    private fun taichi001(revenue: Double) = runCatching {
        val previous = LocalPrefs.totalRevenueFor001
        val current = previous + revenue
        if (current >= 0.01) {
            LocalPrefs.totalRevenueFor001 = 0.0
            EventCenter.logEvent("TotalAdRevenue001", mapOf(FirebaseAnalytics.Param.VALUE to current, FirebaseAnalytics.Param.CURRENCY to "USD"), true)
        } else LocalPrefs.totalRevenueFor001 = current
    }

    protected fun logClick() {
        logState("click")
    }

    protected fun logImpression() {
        logState("impression")
    }
}
