package com.word.file.manager.pdf.base.helper.notice

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.word.file.manager.pdf.EXTRA_DOCUMENT_ACTION_TYPE
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.data.DocumentActionType
import com.word.file.manager.pdf.base.data.DocumentBookmark
import com.word.file.manager.pdf.base.data.DocumentOpenType
import com.word.file.manager.pdf.base.data.PdfCreateType
import com.word.file.manager.pdf.base.utils.isAtLeastAndroid12
import com.word.file.manager.pdf.base.utils.isAtLeastAndroid15
import com.word.file.manager.pdf.base.utils.isGoogleDevice
import com.word.file.manager.pdf.modules.RouteActivity
import kotlin.random.Random

object ToolbarHelper {

    const val TOOLBAR_NOTIFICATION_ID = 30000
    private const val CHANNEL_ID = "Toolbar"

    @SuppressLint("MissingPermission")
    fun buildNotification(): Notification {
        ensureNotificationChannel()
        val builder = NotificationCompat.Builder(app, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_toolbar_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        val largeRemoteViews = buildRemoteViews(R.layout.layout_toolbar_large)
        if (isAtLeastAndroid12()) {
            val tinyRemoteViews = buildRemoteViews(R.layout.layout_toolbar_tiny)
            builder.setCustomContentView(tinyRemoteViews)
                .setCustomBigContentView(largeRemoteViews)
                .setCustomHeadsUpContentView(largeRemoteViews)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        } else {
            builder.setCustomContentView(largeRemoteViews)
                .setCustomBigContentView(largeRemoteViews)
                .setCustomHeadsUpContentView(largeRemoteViews)
        }
        return builder.build().also { notification ->
            runCatching {
                NotificationManagerCompat.from(app).notify(TOOLBAR_NOTIFICATION_ID, notification)
            }
        }
    }

    private fun buildRemoteViews(@LayoutRes layoutId: Int): RemoteViews {
        return RemoteViews(app.packageName, layoutId).apply {
            setOnClickPendingIntent(R.id.layout_scan, openRoute(PdfCreateType))
            setOnClickPendingIntent(R.id.layout_view, openRoute(DocumentOpenType))
            setOnClickPendingIntent(R.id.layout_history, openRoute(DocumentBookmark))
        }
    }

    private fun openRoute(actionType: DocumentActionType): PendingIntent {
        val intent = Intent(app, RouteActivity::class.java).apply {
            putExtra(EXTRA_DOCUMENT_ACTION_TYPE, actionType)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        return PendingIntent.getActivity(app, Random.Default.nextInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun ensureNotificationChannel() {
        val importance =
            if (isAtLeastAndroid15() && isGoogleDevice()) NotificationManagerCompat.IMPORTANCE_MIN else NotificationManagerCompat.IMPORTANCE_DEFAULT
        NotificationManagerCompat.from(app).createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, importance)
                .setName(CHANNEL_ID)
                .setSound(null, null)
                .setLightsEnabled(false)
                .setVibrationEnabled(false)
                .setShowBadge(false)
                .build()
        )
    }
}