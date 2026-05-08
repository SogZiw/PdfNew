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

    private fun JSONArray?.toJsonList(): List<String>? {
        if (this == null) return null
        return buildList {
            for (index in 0 until length()) {
                val item = optString(index).trim()
                if (item.isNotBlank()) add(item)
            }
        }
    }

}