package com.word.file.manager.pdf.base.helper.notice.media

import android.annotation.SuppressLint
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.notice.ContentItems
import com.word.file.manager.pdf.base.helper.notice.NoticeHelper
import com.word.file.manager.pdf.base.helper.notice.NoticeSurface
import com.word.file.manager.pdf.base.helper.notice.NoticeUtils
import com.word.file.manager.pdf.base.helper.notice.NotificationScene
import com.word.file.manager.pdf.base.helper.remote.RemoteLogicConfig

object MediaBuilder {

    private const val CHANNEL_ID = "important_output"

    @SuppressLint("MissingPermission")
    fun showMediaNotice(content: ContentItems, scene: NotificationScene, surface: NoticeSurface) {
        buildChannel()
        EventCenter.logEvent("notification_media_show", mapOf("list" to scene.sceneName))
        if (scene != NotificationScene.UNLOCK && RemoteLogicConfig.fetchFeatureConfig().wakeManager.triggerEnable && NoticeHelper.canWakeToday(RemoteLogicConfig.fetchFeatureConfig().wakeManager.dailyMax)) {
            NoticeUtils.acquireWakeLockByReflect(app, "important")
            NoticeHelper.updateWakeDailyCount()
        }
        val builder = NotificationCompat.Builder(app, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_toolbar_notification)
            .setGroupSummary(false)
            .setGroup("Important")
            .setContentTitle(content.resolveButton(app))
            .setContentText(content.resolveMessage(app))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
            .setContentIntent(NoticeHelper.openRoute(content.actionType, scene, surface))
            .setAutoCancel(true)
        NoticeUtils.setMediaStyleByReflection(app, builder, "mediaSession_output")
        runCatching {
            NotificationManagerCompat.from(app).notify(32000, builder.build())
            NoticeHelper.updateShowRecord(scene, surface)
        }
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


}