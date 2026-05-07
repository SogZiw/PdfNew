package com.word.file.manager.pdf.base.helper.ad

enum class AdType(val reportName: String) {
    AppOpen("open"),
    Interstitial("interstitial"),
    Native("native"),
    Banner("banner")
}

data class AdUnitConfig(
    val placementId: String,
    val provider: String,
    val type: AdType,
    val maxAgeMillis: Long,
    val rank: Int,
)

enum class AdSlot(val jsonKey: String) {
    ColdStart("ad_launch"),
    ScanBreak("ad_scan_int"),
    BackBreak("ad_back_int"),
    MainNative("ad_main_nat"),
    ScanNative("ad_scan_nat"),
    MainBanner("ad_main_ban")
}

enum class NativeAdStyle {
    Media,
    NO_ACTION_MEDIA,
}
