package com.word.file.manager.pdf.base.helper.ad.center

import com.word.file.manager.pdf.base.helper.ad.cache.AdSlotCache
import com.word.file.manager.pdf.base.helper.ad.model.AdSlot
import com.word.file.manager.pdf.base.helper.ad.model.AdType
import com.word.file.manager.pdf.base.helper.ad.model.AdUnitConfig
import org.json.JSONObject

object AdCenter {

    private val slotCaches = AdSlot.entries.associateWith { AdSlotCache(it) }

    val appOpen: AdSlotCache get() = cacheFor(AdSlot.ColdStart)
    val scanInterstitial: AdSlotCache get() = cacheFor(AdSlot.ScanBreak)
    val backInterstitial: AdSlotCache get() = cacheFor(AdSlot.BackBreak)
    val mainNative: AdSlotCache get() = cacheFor(AdSlot.MainNative)
    val scanNative: AdSlotCache get() = cacheFor(AdSlot.ScanNative)
    val mainBanner: AdSlotCache get() = cacheFor(AdSlot.MainBanner)

    fun loadConfig(rawConfig: String = DEFAULT_AD_CONFIG) {
        val source = rawConfig.ifBlank { DEFAULT_AD_CONFIG }
        runCatching {
            val json = JSONObject(source)
            slotCaches.forEach { (slot, cache) -> cache.updateConfigs(json.configsFor(slot)) }
        }
    }

    private fun cacheFor(slot: AdSlot): AdSlotCache {
        return slotCaches.getValue(slot)
    }

    private fun JSONObject.configsFor(slot: AdSlot): List<AdUnitConfig> {
        val array = optJSONArray(slot.jsonKey) ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val type = item.optString("gjkxnj").asAdType() ?: continue
                add(
                    AdUnitConfig(
                        placementId = item.optString("qwedc"),
                        provider = item.optString("kjfnh"),
                        type = type,
                        maxAgeMillis = item.optInt("tgiem", 0) * 1000L,
                        rank = item.optInt("sqjms", 0),
                    ),
                )
            }
        }.sortedWith(compareByDescending { it.rank })
    }

    private fun String.asAdType(): AdType? {
        return when (this) {
            "op" -> AdType.AppOpen
            "int" -> AdType.Interstitial
            "nat" -> AdType.Native
            "ban" -> AdType.Banner
            else -> null
        }
    }

    private const val DEFAULT_AD_CONFIG = """
        {
           "ad_launch":[
              {
                 "qwedc":"ca-app-pub-3940256099942544/9257395921",
                 "kjfnh":"admob",
                 "gjkxnj":"op",
                 "tgiem":13800,
                 "sqjms":3
              }
           ],
           "ad_scan_int":[
              {
                 "qwedc":"ca-app-pub-3940256099942544/1033173712",
                 "kjfnh":"admob",
                 "gjkxnj":"int",
                 "tgiem":3000,
                 "sqjms":3
              }
           ],
           "ad_back_int":[
              {
                 "qwedc":"ca-app-pub-3940256099942544/1033173712",
                 "kjfnh":"admob",
                 "gjkxnj":"int",
                 "tgiem":3000,
                 "sqjms":3
              }
           ],
           "ad_main_nat":[
              {
                 "qwedc":"ca-app-pub-3940256099942544/1044960115",
                 "kjfnh":"admob",
                 "gjkxnj":"nat",
                 "tgiem":3000,
                 "sqjms":3
              }
           ],
           "ad_scan_nat":[
              {
                 "qwedc":"ca-app-pub-3940256099942544/1044960115",
                 "kjfnh":"admob",
                 "gjkxnj":"nat",
                 "tgiem":3000,
                 "sqjms":3
              }
           ],
           "ad_main_ban":[
              {
                 "qwedc":"ca-app-pub-3940256099942544/9214589741",
                 "kjfnh":"admob",
                 "gjkxnj":"ban",
                 "tgiem":3000,
                 "sqjms":3
              }
           ]
        }
    """
}
