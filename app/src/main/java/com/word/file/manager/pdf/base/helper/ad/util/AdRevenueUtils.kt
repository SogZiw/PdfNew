package com.word.file.manager.pdf.base.helper.ad.util

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
import org.json.JSONObject

object AdRevenueUtils {

    fun logPaidValue(slot: AdSlot, config: AdUnitConfig, adValue: AdValue, adapter: AdapterResponseInfo?) {
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
}