package com.word.file.manager.pdf.base.helper.remote

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.isDebug
import org.json.JSONArray
import org.json.JSONObject

object RemoteConfProvider {

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
                RemoteLogicConfig.updatePromotionLogic(it.toPromotionLogic())
            }
            obj.optJSONObject("feature_logic")?.let {
                RemoteLogicConfig.updateFeatureLogic(it.toFeatureLogic())
            }
        }
    }

    private fun JSONObject.toPromotionLogic(): PromotionLogic {
        return PromotionLogic(
            minPreWait = optLong("min_pre_wait", 1000L),
            maxPreWait = optLong("max_pre_wait", 15000L),
            hotStartGap = optLong("hot_start_gap", 1000L),
            fullAdLimit = optLong("full_ad_limit", 3000L),
            exitHomeInt = optFlag("exit_home_int", true),
            overlaySkipInt = optFlag("overlay_skip_int", true),
            initLangInt = optFlag("init_lang_int", true),
            initIntroInt = optFlag("init_intro_int", true),
            initLangNat = optFlag("init_lang_nat", true),
            initIntroNat = optFlag("init_intro_nat", true),
            dashboardNat = optFlag("dashboard_nat", false),
            dashboardBan = optFlag("dashboard_ban", true),
            entryFileInt = optFlag("entry_file_int", true),
            actionSplitInt = optFlag("action_split_int", true),
            reportPageNat = optFlag("report_page_nat", true),
            removeAppInt = optFlag("remove_app_int", true),
            removeAppNatA = optFlag("remove_app_nat_a", true),
            removeAppNatB = optFlag("remove_app_nat_b", true),
            uiThemeSync = optFlag("ui_theme_sync", false),
        )
    }

    private fun JSONObject.toFeatureLogic(): FeatureLogic {
        return FeatureLogic(
            wakeManager = optJSONObject("wake_manager")?.toWakeManagerLogic() ?: WakeManagerLogic(),
            permissionPage = optFlag("permission_page", true),
            serviceKeepAlive = optFlag("service_keep_alive", true),
        )
    }

    private fun JSONObject.toWakeManagerLogic(): WakeManagerLogic {
        return WakeManagerLogic(
            triggerEnable = optFlag("trigger_enable", true),
            dailyMax = optInt("daily_max", 30),
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

    private fun JSONObject.optFlag(name: String, defaultValue: Boolean): Boolean {
        return optInt(name, if (defaultValue) 1 else 0) == 1
    }

}
