package com.word.file.manager.pdf.base.helper

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.helper.remote.RemoteLogicConfig
import com.word.file.manager.pdf.hasGoSettings
import com.word.file.manager.pdf.modules.RouteActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AppLifeManger : Application.ActivityLifecycleCallbacks {

    private val lifecycleScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var backgroundJob: Job? = null
    private var foregroundCount: Int = 0
    private var shouldRelaunchOnForeground: Boolean = false
    private var launchAdClicked: Boolean = false

    fun isAppForeground(): Boolean = foregroundCount > 0

    fun markLaunchAdClicked() {
        launchAdClicked = true
    }

    fun resetLaunchAdClicked() {
        launchAdClicked = false
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityStarted(activity: Activity) {
        backgroundJob?.cancel()
        foregroundCount++
        if (shouldRelaunchOnForeground && openLaunchPageIfNeeded(activity)) {
            shouldRelaunchOnForeground = false
        }
    }

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) {
        foregroundCount = (foregroundCount - 1).coerceAtLeast(0)
        if (foregroundCount > 0) return
        backgroundJob?.cancel()
        backgroundJob = lifecycleScope.launch {
            delay(RemoteLogicConfig.fetchPromotionConfig().hotStartGap)
            if (hasGoSettings) {
                hasGoSettings = false
                return@launch
            }
            shouldRelaunchOnForeground = true
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit

    private fun openLaunchPageIfNeeded(activity: Activity): Boolean {
        if (!app.isScreenInteractive()) return false
        if (activity is RouteActivity && !consumeLaunchAdClicked()) return true
        activity.startActivity(Intent(activity, RouteActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        launchAdClicked = false
        return true
    }

    private fun consumeLaunchAdClicked(): Boolean {
        val clicked = launchAdClicked
        launchAdClicked = false
        return clicked
    }

    private fun Context.isScreenInteractive(): Boolean {
        return getSystemService(PowerManager::class.java)?.isInteractive ?: true
    }
}
