package com.word.file.manager.pdf.base.helper.notice

import android.content.Context
import android.text.format.DateUtils
import androidx.core.content.edit
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.utils.isScreenInteractive
import java.util.Calendar

object NoticeHelper {

    private const val STORE_NAME = "pdf_notice_state"
    private const val KEY_LAST_SHOW_SUFFIX = "LastShowTime"
    private const val KEY_DAILY_COUNT_SUFFIX = "DailyCount"
    private const val KEY_DAILY_TIME_SUFFIX = "DailyCountTime"

    private val noticeState by lazy {
        app.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
    }
    private val firstInstallTime by lazy {
        getInstallReferenceTime()
    }

    var isNoticeOpen: Boolean = false
    var blockStartHour: Int = 0
    var blockEndHour: Int = 0

    var timeConfig: NfConfigItem? = null
    var unlockConfig: NfConfigItem? = null
    var alarmConfig: NfConfigItem? = null

    var isMediaNoticeOpen: Boolean = false
    var mediaTimeConfig: NfConfigItem? = null
    var mediaUnlockConfig: NfConfigItem? = null
    var mediaAlarmConfig: NfConfigItem? = null

    var isWindowNoticeOpen: Boolean = false
    var userNoticePercent: Int = 50
    var windowTimeConfig: NfConfigItem? = null
    var windowUnlockConfig: NfConfigItem? = null

    fun canShowReminder(
        scene: NotificationScene,
        surface: NoticeSurface = NoticeSurface.NORMAL,
        now: Long = System.currentTimeMillis(),
    ): Boolean {
        if (!isSurfaceOpen(surface)) return false
        if (scene == NotificationScene.TIME && isInBlockTime()) return false
        val config = getConfig(scene, surface) ?: return false
        if (!isFirstIntervalReady(config, now)) return false
        val record = getShowRecord(scene, surface)
        val intervalMillis = config.interval.minutesToMillis()
        if (intervalMillis > 0L && (now - record.lastShowTime) < intervalMillis) return false
        if (config.maxCounts > 0 && record.dailyShowCount >= config.maxCounts) return false
        return true
    }

    fun updateShowRecord(
        scene: NotificationScene,
        surface: NoticeSurface = NoticeSurface.NORMAL,
        now: Long = System.currentTimeMillis(),
    ) {
        val key = stateKey(scene, surface)
        val currentCount = getShowRecord(scene, surface).dailyShowCount
        noticeState.edit {
            putLong(key + KEY_LAST_SHOW_SUFFIX, now)
            putInt(key + KEY_DAILY_COUNT_SUFFIX, currentCount + 1)
            putLong(key + KEY_DAILY_TIME_SUFFIX, now)
        }
    }

    private fun getConfig(
        scene: NotificationScene,
        surface: NoticeSurface = NoticeSurface.NORMAL,
    ): NfConfigItem? {
        return when (surface) {
            NoticeSurface.NORMAL -> when (scene) {
                NotificationScene.TIME -> timeConfig
                NotificationScene.UNLOCK -> unlockConfig
                NotificationScene.ALARM -> alarmConfig
            }

            NoticeSurface.MEDIA -> when (scene) {
                NotificationScene.TIME -> mediaTimeConfig
                NotificationScene.UNLOCK -> mediaUnlockConfig
                NotificationScene.ALARM -> mediaAlarmConfig
            }

            NoticeSurface.WINDOW -> when (scene) {
                NotificationScene.TIME -> windowTimeConfig
                NotificationScene.UNLOCK -> windowUnlockConfig
                NotificationScene.ALARM -> null
            }
        }
    }

    private fun getShowRecord(
        scene: NotificationScene,
        surface: NoticeSurface = NoticeSurface.NORMAL,
    ): NoticeShowRecord {
        val key = stateKey(scene, surface)
        resetDailyCountIfNeeded(key)
        return NoticeShowRecord(
            lastShowTime = noticeState.getLong(key + KEY_LAST_SHOW_SUFFIX, 0L),
            dailyShowCount = noticeState.getInt(key + KEY_DAILY_COUNT_SUFFIX, 0),
            dailyCountTime = noticeState.getLong(key + KEY_DAILY_TIME_SUFFIX, 0L),
        )
    }

    private fun isSurfaceOpen(surface: NoticeSurface): Boolean {
        return when (surface) {
            NoticeSurface.NORMAL -> isNoticeOpen
            NoticeSurface.MEDIA -> isMediaNoticeOpen
            NoticeSurface.WINDOW -> isWindowNoticeOpen
        }
    }

    private fun isFirstIntervalReady(config: NfConfigItem, now: Long): Boolean {
        val firstMillis = config.first.minutesToMillis()
        if (firstMillis <= 0L) return true
        return now - firstInstallTime >= firstMillis
    }

    private fun getInstallReferenceTime(): Long {
        return runCatching {
            app.packageManager.getPackageInfo(app.packageName, 0).firstInstallTime.takeIf { it > 0L }
        }.getOrNull() ?: 0L
    }

    private fun resetDailyCountIfNeeded(key: String) {
        val dailyTimeKey = key + KEY_DAILY_TIME_SUFFIX
        val dailyCountTime = noticeState.getLong(dailyTimeKey, 0L)
        if (dailyCountTime <= 0L || DateUtils.isToday(dailyCountTime)) return
        noticeState.edit {
            putInt(key + KEY_DAILY_COUNT_SUFFIX, 0)
            putLong(dailyTimeKey, System.currentTimeMillis())
        }
    }

    private fun isInBlockTime(): Boolean {
        if (blockStartHour == blockEndHour || app.isScreenInteractive()) return false
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return if (blockEndHour > blockStartHour) {
            currentHour in blockStartHour until blockEndHour
        } else {
            currentHour >= blockStartHour || currentHour in 0 until blockEndHour
        }
    }

    private fun stateKey(scene: NotificationScene, surface: NoticeSurface): String {
        return "${surface.storeName}_${scene.sceneName}"
    }

    private fun Int.minutesToMillis(): Long {
        return this * 60_000L
    }
}
