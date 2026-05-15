package com.word.file.manager.pdf.base.helper.remote

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.word.file.manager.pdf.base.data.DocumentActionType
import com.word.file.manager.pdf.base.data.DocumentBookmark
import com.word.file.manager.pdf.base.data.DocumentOpenType
import com.word.file.manager.pdf.base.data.DocumentTools
import com.word.file.manager.pdf.base.data.PdfCreateType
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.base.helper.notice.ContentItems
import com.word.file.manager.pdf.base.helper.notice.NfConfigItem
import com.word.file.manager.pdf.base.helper.notice.NoticeContentManager
import com.word.file.manager.pdf.base.helper.notice.NoticeHelper
import com.word.file.manager.pdf.base.utils.isSamsungDevice
import com.word.file.manager.pdf.base.utils.showLog
import com.word.file.manager.pdf.isDebug
import org.json.JSONArray
import org.json.JSONObject

object RemoteConfProvider {

    private const val REMOTE_NOTIFICATION_ID_BASE = 32000

    private val remoteConfig by lazy {
        Firebase.remoteConfig.apply { setConfigSettingsAsync(remoteConfigSettings { minimumFetchIntervalInSeconds = 3600 }) }
    }

    fun init() {
        if (isDebug) {
            AdCenter.loadConfig()
            return
        }
        initAllConfigs()
        remoteConfig.fetchAndActivate().addOnSuccessListener {
            initAllConfigs()
        }
    }

    private fun initAllConfigs() {
        getRemoteAdJson()
        getUserControlJson()
        getAppSwitchJson()
        getRemoteFakePkg()
        getRemoteNfContent()
        getRemotePopNoticeConfig()
        getRemoteUseLegacyChannel()
        getServerTimeConf()
        getChannelConfig()
    }

    private fun getChannelConfig() {
        runCatching {
            val json = remoteConfig["channel_config"].asString()
            if (json.isBlank()) return
            val obj = JSONObject(json)
            NoticeHelper.run {
                useChannelDynamic = 1 == obj.optInt("switch", 1)
                channelDynamicInterval = obj.optInt("interval", 24) * 60 * 60 * 1000L
                channelMaxCounts = obj.optInt("max", 5)
            }
        }
    }

    private fun getServerTimeConf() {
        runCatching {
            RemoteLogicConfig.isUseServerTime = 1 == (remoteConfig["server_time"].asString().toIntOrNull() ?: 1)
        }
    }

    private fun getRemoteUseLegacyChannel() {
        runCatching {
            NoticeHelper.useLegacyChannel = 1 == (remoteConfig["legacy_channel"].asString().toIntOrNull() ?: 1)
        }
    }

    private fun getRemoteFakePkg() {
        runCatching {
            UserBlockHelper.isUseFakeBlock = 1 == (remoteConfig["fake_switch"].asString().toIntOrNull() ?: 1)
        }
    }

    private fun getRemoteNfContent() {
        val contentList = runCatching {
            val json = remoteConfig["agile_noti_text"].asString()
            if (json.isBlank()) return@runCatching emptyList()
            JSONArray(json).toRemoteContentGroups()
        }.getOrElse { error ->
            "Parse agile_noti_text failed: ${error.message.orEmpty()}".showLog("RemoteConfProvider")
            emptyList()
        }
        NoticeContentManager.updateRemoteContentList(contentList)
    }

    private fun getRemotePopNoticeConfig() {
        runCatching {
            val json = remoteConfig[if (isSamsungDevice()) "agile_pop_noti_sg" else "agile_pop_noti"].asString()
            if (json.isBlank()) return@runCatching
            JSONObject(json).apply {
                NoticeHelper.isNoticeOpen = optInt("open", 0) == 1
                NoticeHelper.blockStartHour = optInt("start", 0)
                NoticeHelper.blockEndHour = optInt("end", 0)
                NoticeHelper.timeConfig = optJSONObject("agile_time").toNoticeConfig()
                NoticeHelper.unlockConfig = optJSONObject("agile_unlock").toNoticeConfig()
                NoticeHelper.alarmConfig = optJSONObject("agile_alarm").toNoticeConfig()

                NoticeHelper.isMediaNoticeOpen = optInt("open_media", 0) == 1
                NoticeHelper.mediaTimeConfig = optJSONObject("agile_time_media").toNoticeConfig()
                NoticeHelper.mediaUnlockConfig = optJSONObject("agile_unlock_media").toNoticeConfig()
                NoticeHelper.mediaAlarmConfig = optJSONObject("agile_alarm_media").toNoticeConfig()

                NoticeHelper.isWindowNoticeOpen = optInt("win_open", 0) == 1
                NoticeHelper.userNoticePercent = optInt("user_not", 50)
                NoticeHelper.windowTimeConfig = optJSONObject("agile_time_w").toNoticeConfig()
                NoticeHelper.windowUnlockConfig = optJSONObject("agile_unlock_w").toNoticeConfig()
            }
        }
    }

    private fun getRemoteAdJson() {
        runCatching {
            AdCenter.loadConfig(remoteConfig["ad_config"].asString())
        }
    }

    private fun getUserControlJson() {
        runCatching {
            val json = remoteConfig["config_user"].asString()
            if (json.isBlank()) return@runCatching
            val obj = JSONObject(json)
            obj.optJSONObject("user_refr")?.let { userReferrer ->
                UserBlockHelper.updateAllowedMarks(
                    open = userReferrer.optInt("open", 1) == 1,
                    marks = userReferrer.optJSONArray("string").toJsonList(),
                )
            }
            UserBlockHelper.updateBlockedMarks(obj.optJSONArray("user_back").toJsonList())
        }
    }

    private fun getAppSwitchJson() {
        runCatching {
            val json = remoteConfig["agile_user_app"].asString()
            if (json.isBlank()) return@runCatching
            val obj = JSONObject(json)
            obj.optJSONObject("promotion_logic")?.let {
                RemoteLogicConfig.updatePromotionLogicWhite(it.toPromotionLogic())
            }
            obj.optJSONObject("feature_logic")?.let {
                RemoteLogicConfig.updateFeatureLogicWhite(it.toFeatureLogic())
            }
        }
        runCatching {
            val json = remoteConfig["agile_user_test"].asString()
            if (json.isBlank()) return@runCatching
            val obj = JSONObject(json)
            obj.optJSONObject("promotion_logic")?.let {
                RemoteLogicConfig.updatePromotionLogic(it.toPromotionLogic(PromotionLogic.blockedUserDefaults()))
            }
            obj.optJSONObject("feature_logic")?.let {
                RemoteLogicConfig.updateFeatureLogic(it.toFeatureLogic(FeatureLogic.blockedUserDefaults()))
            }
        }
    }

    private fun JSONObject.toPromotionLogic(defaults: PromotionLogic = PromotionLogic()): PromotionLogic {
        return PromotionLogic(
            minPreWait = optLong("min_pre_wait", defaults.minPreWait),
            maxPreWait = optLong("max_pre_wait", defaults.maxPreWait),
            hotStartGap = optLong("hot_start_gap", defaults.hotStartGap),
            fullAdLimit = optLong("full_ad_limit", defaults.fullAdLimit),
            exitHomeInt = optFlag("exit_home_int", defaults.exitHomeInt),
            overlaySkipInt = optFlag("overlay_skip_int", defaults.overlaySkipInt),
            initLangInt = optFlag("init_lang_int", defaults.initLangInt),
            initIntroInt = optFlag("init_intro_int", defaults.initIntroInt),
            initLangNat = optFlag("init_lang_nat", defaults.initLangNat),
            initIntroNat = optFlag("init_intro_nat", defaults.initIntroNat),
            dashboardNat = optFlag("dashboard_nat", defaults.dashboardNat),
            dashboardBan = optFlag("dashboard_ban", defaults.dashboardBan),
            entryFileInt = optFlag("entry_file_int", defaults.entryFileInt),
            actionSplitInt = optFlag("action_split_int", defaults.actionSplitInt),
            reportPageNat = optFlag("report_page_nat", defaults.reportPageNat),
            removeAppInt = optFlag("remove_app_int", defaults.removeAppInt),
            removeAppNatA = optFlag("remove_app_nat_a", defaults.removeAppNatA),
            removeAppNatB = optFlag("remove_app_nat_b", defaults.removeAppNatB),
            uiThemeSync = optFlag("ui_theme_sync", defaults.uiThemeSync),
        )
    }

    private fun JSONObject.toFeatureLogic(defaults: FeatureLogic = FeatureLogic()): FeatureLogic {
        return FeatureLogic(
            wakeManager = optJSONObject("wake_manager")?.toWakeManagerLogic(defaults.wakeManager) ?: defaults.wakeManager,
            firstShow = optJSONObject("fir_show")?.toFirstShowLogic(defaults.firstShow) ?: defaults.firstShow,
            permissionPage = optFlag("permission_page", defaults.permissionPage),
            serviceKeepAlive = optFlag("service_keep_alive", defaults.serviceKeepAlive),
        )
    }

    private fun JSONObject.toFirstShowLogic(defaults: FirstShowLogic = FirstShowLogic()): FirstShowLogic {
        return FirstShowLogic(
            pageLang = optFlag("page_lang", defaults.pageLang),
            pageIntro = optFlag("page_intro", defaults.pageIntro),
        )
    }

    private fun JSONObject.toWakeManagerLogic(defaults: WakeManagerLogic = WakeManagerLogic()): WakeManagerLogic {
        return WakeManagerLogic(
            triggerEnable = optFlag("trigger_enable", defaults.triggerEnable),
            dailyMax = optInt("daily_max", defaults.dailyMax),
        )
    }

    private fun JSONArray?.toJsonList(): List<String>? {
        if (this == null) return null
        return buildList {
            for (index in 0 until length()) {
                val item = optString(index).trim()
                if (item.isNotBlank()) add(item)
            }
        }
    }

    private fun JSONArray.toRemoteContentGroups(): List<List<ContentItems>> {
        val groups = mutableListOf<List<ContentItems>>()
        for (groupIndex in 0 until length()) {
            val groupObj = optJSONObject(groupIndex) ?: continue
            val actionType = groupObj.optString("page").toNoticeActionType() ?: continue
            val textItems = groupObj.optJSONArray("noti_text") ?: continue
            val notificationId = REMOTE_NOTIFICATION_ID_BASE + groups.size
            val groupItems = mutableListOf<ContentItems>()
            for (itemIndex in 0 until textItems.length()) {
                val itemObj = textItems.optJSONObject(itemIndex) ?: continue
                val message = itemObj.optString("text").trim()
                val button = itemObj.optString("button").trim()
                if (message.isBlank() || button.isBlank()) continue
                groupItems.add(
                    ContentItems(
                        message = 0,
                        button = 0,
                        actionType = actionType,
                        notificationId = notificationId,
                        messageText = message,
                        buttonText = button,
                    )
                )
            }
            if (groupItems.isNotEmpty()) groups.add(groupItems)
        }
        return groups
    }

    private fun JSONObject?.toNoticeConfig(): NfConfigItem? {
        if (this == null) return null
        return NfConfigItem(
            first = optInt("agile_fi", 0),
            interval = optInt("agile_mi", 30),
            maxCounts = optInt("agile_up", 30),
        )
    }

    private fun String.toNoticeActionType(): DocumentActionType? {
        return when (trim().lowercase()) {
            "view" -> DocumentOpenType
            "history" -> DocumentBookmark
            "scan" -> PdfCreateType
            "tools" -> DocumentTools
            else -> null
        }
    }

    private fun JSONObject.optFlag(name: String, defaultValue: Boolean): Boolean {
        return optInt(name, if (defaultValue) 1 else 0) == 1
    }

}
