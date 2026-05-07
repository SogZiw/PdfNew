package com.word.file.manager.pdf.base.helper.remote

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.isDebug

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
    }

    private fun getRemoteAdJson() {
        runCatching {
            AdCenter.loadConfig(remoteConfig["ad_config"].asString())
        }
    }

}