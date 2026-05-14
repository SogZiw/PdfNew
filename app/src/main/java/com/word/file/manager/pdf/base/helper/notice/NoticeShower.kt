package com.word.file.manager.pdf.base.helper.notice

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.app.AlarmManagerCompat
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.helper.AppLifeManger
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.LocalPrefs
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.notice.extra.ExtraBuilder
import com.word.file.manager.pdf.base.helper.notice.media.MediaBuilder
import com.word.file.manager.pdf.base.helper.notice.normal.NormalBuilder
import com.word.file.manager.pdf.base.helper.receivers.RebornReceiver
import com.word.file.manager.pdf.base.helper.remote.RemoteLogicConfig
import com.word.file.manager.pdf.base.helper.remote.TrustedTimeHolder
import com.word.file.manager.pdf.base.helper.services.TaskJobIntentService
import com.word.file.manager.pdf.base.utils.buildPeriodicSignalFlow
import com.word.file.manager.pdf.modules.permissions.hasOverlayPermission
import com.word.file.manager.pdf.modules.permissions.hasPostNotificationPermission
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

object NoticeShower {

    val workScope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineExceptionHandler { _, _ -> }) }

    private val alarmManager by lazy { app.getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    private val unlockReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                workScope.launch {
                    delay(1000L)
                    showNotice(NotificationScene.UNLOCK)
                    context?.let {
                        runCatching {
                            TaskJobIntentService.work(context)
                        }
                    }
                }
            }
        }
    }


    fun startListeners() {
        app.registerReceiver(unlockReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
        })
        scheduleNextAlarm()
        workScope.launch {
            buildPeriodicSignalFlow(intervalMs = 60000L, firstDelayMs = 10000L).collect {
                showNotice(NotificationScene.TIME)
            }
        }
        workScope.launch {
            buildPeriodicSignalFlow(intervalMs = 10 * 60000L, firstDelayMs = 1000L).collect {
                EventCenter.logEvent("session_back")
            }
        }
    }

    fun scheduleNextAlarm() {
        runCatching {
            val interval = NoticeHelper.getAlarmInterval()
            if (interval <= 0) return
            if (LocalPrefs.nextSetAlarmTime > System.currentTimeMillis()) return
            val pendingIntent = PendingIntent.getBroadcast(
                app,
                Random.nextInt(1000, 1500),
                Intent(app, RebornReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
            val nextTime = System.currentTimeMillis() + interval * 60000L
            LocalPrefs.nextSetAlarmTime = nextTime
            AlarmManagerCompat.setAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                nextTime,
                pendingIntent
            )
        }
    }

    fun showNotice(scene: NotificationScene) {
        if (AppLifeManger.isAppForeground()) return
        if (NoticeHelper.isWindowNoticeOpen && UserBlockHelper.canShowExtra() && hasOverlayPermission() && NoticeHelper.canShowReminder(scene, NoticeSurface.WINDOW)) {
            showBySurface(scene, NoticeSurface.WINDOW)
            return
        }
        if (hasPostNotificationPermission()) {
            if (NoticeHelper.canShowReminder(scene, NoticeSurface.NORMAL)) {
                showBySurface(scene, NoticeSurface.NORMAL)
            }
        } else {
            if (UserBlockHelper.canShowExtra() && NoticeHelper.canShowReminder(scene, NoticeSurface.MEDIA)) {
                showBySurface(scene, NoticeSurface.MEDIA)
            }
        }
    }

    private fun showBySurface(scene: NotificationScene, surface: NoticeSurface) {
        workScope.launch {
            if (RemoteLogicConfig.isUseServerTime) {
                val serverTime = TrustedTimeHolder.fetchServiceTime() ?: return@launch
                if (serverTime == 0L) return@launch
                if (!TrustedTimeHolder.isWithinFiveMinutesOfNow(serverTime)) return@launch
            }
            val content = NoticeContentManager.getCurrentItems() ?: return@launch
            withContext(Dispatchers.Main) {
                when (surface) {
                    NoticeSurface.NORMAL -> NormalBuilder.showNormalNotice(content, scene, surface)
                    NoticeSurface.MEDIA -> MediaBuilder.showMediaNotice(content, scene, surface)
                    NoticeSurface.WINDOW -> ExtraBuilder.showExtraView(content, scene, surface)
                }
            }
        }
    }
}