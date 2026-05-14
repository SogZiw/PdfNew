package com.word.file.manager.pdf.base.helper.notice

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.helper.AppLifeManger
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.modules.permissions.hasOverlayPermission
import com.word.file.manager.pdf.modules.permissions.hasPostNotificationPermission

object NoticeShower {

    private const val CHANNEL_ID = "important_message"

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
            if (NoticeHelper.canShowReminder(scene, NoticeSurface.MEDIA)) {
                showBySurface(scene, NoticeSurface.MEDIA)
            }
        }
    }

    fun showBySurface(scene: NotificationScene, surface: NoticeSurface) {
        val content = NoticeContentManager.getCurrentItems() ?: return

    }

    fun showNormalNotice(content: ContentItems, scene: NotificationScene, surface: NoticeSurface) {
        if (NoticeHelper.useLegacyChannel) buildChannelForLow() else buildChannel()
        EventCenter.logEvent("notification_popup_show", mapOf("list" to scene.sceneName))
    }

    private fun buildChannel() {
        NotificationManagerCompat.from(app).createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_MAX)
                .setLightsEnabled(true)
                .setVibrationEnabled(true)
                .setShowBadge(true)
                .setName(CHANNEL_ID)
                .build()
        )
    }

    @SuppressLint("WrongConstant")
    private fun buildChannelForLow() {
        runCatching {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_MAX).apply {
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = app.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}