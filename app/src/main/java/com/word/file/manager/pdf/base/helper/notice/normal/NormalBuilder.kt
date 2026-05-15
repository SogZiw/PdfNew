package com.word.file.manager.pdf.base.helper.notice.normal

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.notice.ChannelBuilder
import com.word.file.manager.pdf.base.helper.notice.ContentItems
import com.word.file.manager.pdf.base.helper.notice.NoticeHelper
import com.word.file.manager.pdf.base.helper.notice.NoticeHelper.channelDynamicInterval
import com.word.file.manager.pdf.base.helper.notice.NoticeHelper.channelMaxCounts
import com.word.file.manager.pdf.base.helper.notice.NoticeHelper.useChannelDynamic
import com.word.file.manager.pdf.base.helper.notice.NoticeSurface
import com.word.file.manager.pdf.base.helper.notice.NoticeUtils
import com.word.file.manager.pdf.base.helper.notice.NotificationScene
import com.word.file.manager.pdf.base.helper.remote.RemoteLogicConfig
import com.word.file.manager.pdf.base.utils.isAtLeastAndroid15
import com.word.file.manager.pdf.base.utils.isGoogleDevice
import com.word.file.manager.pdf.base.utils.isOnePlusDevice
import com.word.file.manager.pdf.base.utils.isSamsungDevice
import com.word.file.manager.pdf.base.utils.isXiaoMiDevice

object NormalBuilder {

    private const val CHANNEL_ID = "important_message"

    @SuppressLint("MissingPermission")
    fun showNormalNotice(content: ContentItems, scene: NotificationScene, surface: NoticeSurface) {
        val channelId = if (useChannelDynamic) {
            ChannelBuilder.createDynamicChannelIfNeeded(channelDynamicInterval, channelMaxCounts, CHANNEL_ID)
        } else CHANNEL_ID

        if (NoticeHelper.useLegacyChannel) buildChannelForLow(channelId) else buildChannel(channelId)
        EventCenter.logEvent("notification_popup_show", mapOf("list" to scene.sceneName))
        if (scene != NotificationScene.UNLOCK
            && UserBlockHelper.canShowExtra()
            && RemoteLogicConfig.fetchFeatureConfig().wakeManager.triggerEnable
            && NoticeHelper.canWakeToday(RemoteLogicConfig.fetchFeatureConfig().wakeManager.dailyMax)
        ) {
            NoticeUtils.acquireWakeLockByReflect(app, "important")
            NoticeHelper.updateWakeDailyCount()
        }
        val builder = NotificationCompat.Builder(app, channelId)
            .setSmallIcon(R.drawable.ic_toolbar_notification)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentTitle(content.resolveButton(app))
            .setContentText(content.resolveMessage(app))
            .setContentIntent(NoticeHelper.openRoute(content.actionType, scene, surface))
            .setGroupSummary(false)
            .setGroup("Important")
        if (UserBlockHelper.canShowExtra()) {
            builder.setWhen(System.currentTimeMillis() + channelDynamicInterval)
            builder.setOngoing(true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val tiny = buildRemoteViews(content, R.layout.layout_notice_small)
            val large = buildRemoteViews(content, R.layout.layout_notice_large)
            builder.setCustomContentView(tiny).setCustomHeadsUpContentView(tiny).setCustomBigContentView(large)
            builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        } else {
            val large = buildRemoteViews(content, R.layout.layout_notice_large)
            if (isSamsungDevice() || isXiaoMiDevice() || isOnePlusDevice()) {
                val mid = buildRemoteViews(content, R.layout.layout_notice_mid)
                builder.setCustomContentView(mid).setCustomHeadsUpContentView(mid).setCustomBigContentView(large)
            } else {
                builder.setCustomContentView(large).setCustomHeadsUpContentView(large).setCustomBigContentView(large)
            }
        }
        runCatching {
            val notificationId = if (isAtLeastAndroid15() && isGoogleDevice()) 32000 else content.notificationId
            NotificationManagerCompat.from(app).notify(notificationId, builder.build())
            NoticeHelper.updateShowRecord(scene, surface)
        }
    }

    private fun buildRemoteViews(content: ContentItems, layoutId: Int): RemoteViews {
        return RemoteViews(app.packageName, layoutId).apply {
            setImageViewResource(R.id.image_icon, content.actionType.getShortcutsIconRes())
            setTextViewText(R.id.text_content, content.resolveMessage(app))
            setTextViewText(R.id.text_button, content.resolveButton(app))
        }
    }

    private fun buildChannel(channelId: String) {
        NotificationManagerCompat.from(app).createNotificationChannel(
            NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_MAX)
                .setLightsEnabled(true)
                .setVibrationEnabled(true)
                .setShowBadge(true)
                .setName(CHANNEL_ID)
                .build()
        )
    }

    @SuppressLint("WrongConstant")
    private fun buildChannelForLow(channelId: String) {
        runCatching {
            val channel = NotificationChannel(channelId, CHANNEL_ID, NotificationManager.IMPORTANCE_MAX).apply {
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