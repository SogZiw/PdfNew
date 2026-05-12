package com.word.file.manager.pdf.modules

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.word.file.manager.pdf.AD_POS_ID
import com.word.file.manager.pdf.APP_AD_CHANCE
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.helper.AppLifeManger
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.LocalPrefs
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.base.helper.ad.model.AdSlot
import com.word.file.manager.pdf.base.helper.net.BaseInfo
import com.word.file.manager.pdf.base.helper.net.NetworkCenter
import com.word.file.manager.pdf.base.helper.remote.RemoteLogicConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RouteViewModel : ViewModel() {

    val afterUMPLiveData = MutableLiveData<Boolean>()
    val nextJobLiveData = MutableLiveData<Boolean>()

    private var waitLoadingJob: Job? = null
    private var hasDispatchedNext = false

    fun beginLaunch(activity: AppCompatActivity) {
        NetworkCenter.session()
        EventCenter.logEvent(LOADING_SHOW)
        fetchUMPResult(activity)
    }

    fun startLoadAd(logEvent: Boolean) {
        if (logEvent) EventCenter.logEvent(APP_AD_CHANCE, mapOf(AD_POS_ID to AdSlot.ColdStart.jsonKey))
        AdCenter.appOpen.preload()
        AdCenter.scanInterstitial.preload()
        if (shouldPreloadMainNative()) {
            AdCenter.mainNative.preload()
        } else {
            AdCenter.scanNative.preload()
        }
    }

    private fun shouldPreloadMainNative(): Boolean {
        return LocalPrefs.hasSeenIntroduce.not() || RemoteLogicConfig.fetchPromotionConfig().dashboardNat
    }

    fun startLoadingLaunch(activity: BaseActivity<*>) {
        startLoadAd(logEvent = true)
        waitLoadingJob?.cancel()
        waitLoadingJob = viewModelScope.launch {
            var elapsedMs = 0L
            while (elapsedMs < RemoteLogicConfig.fetchPromotionConfig().maxPreWait) {
                delay(LOADING_STEP_MS)
                elapsedMs += LOADING_STEP_MS
                if (elapsedMs % RELOAD_AD_STEP_MS == 0L) startLoadAd(logEvent = false)
                if (activity.fetchResumeState() && elapsedMs >= RemoteLogicConfig.fetchPromotionConfig().minPreWait && AdCenter.appOpen.hasCachedAd()) {
                    waitLoadingJob?.cancel()
                    AppLifeManger.resetLaunchAdClicked()
                    AdCenter.appOpen.showFullScreen(
                        activity = activity,
                        eventName = AdSlot.ColdStart.jsonKey,
                        closed = { dispatchNextPage() },
                        clicked = { AppLifeManger.markLaunchAdClicked() },
                    )
                    return@launch
                }
            }
            dispatchNextPage()
        }
    }

    fun logNoticeRequest(tag: String) {
        EventCenter.logEvent(NOTIFY_PERMISSION_SHOW, mapOf("list" to tag))
    }

    private fun fetchUMPResult(activity: AppCompatActivity) {
        if (LocalPrefs.hasCheckedLaunchConsent) {
            afterUMPLiveData.postValue(true)
            return
        }

        LocalPrefs.hasCheckedLaunchConsent = true
        if (!EU_COUNTRIES.contains(BaseInfo.firstDeviceCountry.uppercase())) {
            afterUMPLiveData.postValue(false)
            return
        }

        runCatching {
            UserMessagingPlatform.getConsentInformation(activity)
                .requestConsentInfoUpdate(
                    activity, ConsentRequestParameters.Builder().build(),
                    {
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                            afterUMPLiveData.postValue(true)
                        }
                    },
                    {
                        afterUMPLiveData.postValue(false)
                    },
                )
        }.onFailure {
            afterUMPLiveData.postValue(false)
        }
    }

    private fun dispatchNextPage() {
        if (hasDispatchedNext) return
        hasDispatchedNext = true
        nextJobLiveData.postValue(true)
    }

    override fun onCleared() {
        waitLoadingJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val LOADING_STEP_MS = 100L
        const val RELOAD_AD_STEP_MS = 1_000L
        const val LOADING_SHOW = "loading_show"
        const val NOTIFY_PERMISSION_SHOW = "notify_permission_show"

        val EU_COUNTRIES = setOf(
            "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IE", "IT", "LV",
            "LT", "LU", "MT", "NL", "PL", "PT", "RO", "SK", "SI", "ES", "SE", "NO", "IS", "LI", "CH", "GB",
        )
    }
}
