package com.word.file.manager.pdf.base.helper.remote

import com.word.file.manager.pdf.base.helper.UserBlockHelper

object RemoteLogicConfig {

    var isUseServerTime = true

    var promotionLogic: PromotionLogic = PromotionLogic.blockedUserDefaults()
        private set

    var featureLogic: FeatureLogic = FeatureLogic.blockedUserDefaults()
        private set

    var promotionLogicForWhite: PromotionLogic = PromotionLogic()
        private set

    var featureLogicForWhite: FeatureLogic = FeatureLogic()
        private set

    fun updatePromotionLogicWhite(logic: PromotionLogic) {
        promotionLogicForWhite = logic
    }

    fun updateFeatureLogicWhite(logic: FeatureLogic) {
        featureLogicForWhite = logic
    }

    fun updatePromotionLogic(logic: PromotionLogic) {
        promotionLogic = logic
    }

    fun updateFeatureLogic(logic: FeatureLogic) {
        featureLogic = logic
    }

    fun fetchPromotionConfig() = if (UserBlockHelper.canShowExtra()) promotionLogicForWhite else promotionLogic
    fun fetchFeatureConfig() = if (UserBlockHelper.canShowExtra()) featureLogicForWhite else featureLogic
}

data class PromotionLogic(
    /** Loading预加载最小等待时间，单位毫秒。 */
    val minPreWait: Long = 1000L,

    /** Loading预加载最大等待时间，单位毫秒。 */
    val maxPreWait: Long = 15000L,

    /** 热启动间隔，单位毫秒。 */
    val hotStartGap: Long = 1000L,

    /** 全屏广告类型的展示间隔，包含开屏和插屏，单位毫秒。 */
    val fullAdLimit: Long = 3000L,

    /** 返回主页插屏，false为不展示，true为展示。 */
    val exitHomeInt: Boolean = true,

    /** 悬浮窗授权页跳过插屏，false为不展示，true为展示。 */
    val overlaySkipInt: Boolean = true,

    /** 新用户语言页插屏，false为不展示，true为展示。 */
    val initLangInt: Boolean = true,

    /** 新用户介绍页插屏，false为不展示，true为展示。 */
    val initIntroInt: Boolean = true,

    /** 新用户语言页原生，false为不展示，true为展示。 */
    val initLangNat: Boolean = true,

    /** 新用户介绍页原生，false为不展示，true为展示。 */
    val initIntroNat: Boolean = true,

    /** 首页原生\Tools页面原生，false为不展示，true为展示。 */
    val dashboardNat: Boolean = true,

    /** 首页banner，false为不展示，true为展示。 */
    val dashboardBan: Boolean = true,

    /** 进入文件插屏，false为不展示，true为展示。 */
    val entryFileInt: Boolean = true,

    /** 合并/拆分插屏，false为不展示，true为展示。 */
    val actionSplitInt: Boolean = true,

    /** 结果页原生，false为不展示，true为展示。 */
    val reportPageNat: Boolean = true,

    /** 卸载页插屏，false为不展示，true为展示。 */
    val removeAppInt: Boolean = true,

    /** 卸载页原生1，false为不展示，true为展示。 */
    val removeAppNatA: Boolean = true,

    /** 卸载页原生2，false为不展示，true为展示。 */
    val removeAppNatB: Boolean = true,

    /** 原生广告按钮是否与主题色一致，false为不一致，true为一致。 */
    val uiThemeSync: Boolean = true,
) {
    companion object {
        fun blockedUserDefaults() = PromotionLogic(
            exitHomeInt = false,
            overlaySkipInt = false,
            initIntroInt = false,
            initLangNat = false,
            initIntroNat = false,
            uiThemeSync = false,
        )
    }
}

data class FeatureLogic(
    /** 通知屏幕唤醒频率控制配置。 */
    val wakeManager: WakeManagerLogic = WakeManagerLogic(),

    /** 首次引导页面展示控制配置。 */
    val firstShow: FirstShowLogic = FirstShowLogic(),

    /** 悬浮窗授权页面是否展示，false为不展示，true为展示。 */
    val permissionPage: Boolean = true,

    /** 前台服务开启控制，false为不启动，true为启动；下发后已启动则不关闭当前服务。 */
    val serviceKeepAlive: Boolean = true,
) {
    companion object {
        fun blockedUserDefaults() = FeatureLogic(
            wakeManager = WakeManagerLogic(dailyMax = 20),
            permissionPage = false,
        )
    }
}

data class FirstShowLogic(
    /** 首次引导多语言页面是否展示，false为不展示，true为展示。 */
    val pageLang: Boolean = true,

    /** 首次引导介绍页面是否展示，false为不展示，true为展示。 */
    val pageIntro: Boolean = true,
)

data class WakeManagerLogic(
    /** 通知屏幕唤醒开关，false表示不开启，true表示开启。 */
    val triggerEnable: Boolean = true,

    /** 自然日通知唤醒上限，仅针对屏幕不可交互的情况。 */
    val dailyMax: Int = 40,
)
