package com.word.file.manager.pdf.base.helper.services

import android.app.Application
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.word.file.manager.pdf.base.helper.net.BaseInfo
import com.word.file.manager.pdf.base.helper.notice.ToolbarHelper
import com.word.file.manager.pdf.base.helper.remote.RemoteLogicConfig
import com.word.file.manager.pdf.base.utils.isAtLeastAndroid12
import com.word.file.manager.pdf.base.utils.isSamsungDevice

class CoreService : Service() {

    companion object {
        var isToolbarRunning = false

        fun showToolbar(context: Context, force: Boolean = false) {
            if (!RemoteLogicConfig.fetchFeatureConfig().serviceKeepAlive) return
            if ("KR" == BaseInfo.firstDeviceCountry && isSamsungDevice()) return
            if (force) {
                runCatching {
                    if (isToolbarRunning) {
                        if (isSamsungDevice()) return
                        if (NotificationManagerCompat.from(context).activeNotifications.all { it.id != ToolbarHelper.TOOLBAR_NOTIFICATION_ID }) {
                            ToolbarHelper.buildNotification()
                        }
                        return
                    }
                    ContextCompat.startForegroundService(context, Intent(context, CoreService::class.java))
                }
            } else {
                if (isAtLeastAndroid12() && context is Application) {
                    ToolbarHelper.buildNotification()
                } else {
                    runCatching {
                        if (isToolbarRunning) {
                            if (isSamsungDevice()) return
                            if (NotificationManagerCompat.from(context).activeNotifications.all { it.id != ToolbarHelper.TOOLBAR_NOTIFICATION_ID }) {
                                ToolbarHelper.buildNotification()
                            }
                            return
                        }
                        ContextCompat.startForegroundService(context, Intent(context, CoreService::class.java))
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        isToolbarRunning = true
    }

    private fun buildToolbar() {
        runCatching {
            val notification = ToolbarHelper.buildNotification()
            startForeground(ToolbarHelper.TOOLBAR_NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        buildToolbar()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isToolbarRunning = false
    }

}