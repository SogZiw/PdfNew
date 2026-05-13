package com.word.file.manager.pdf.base.helper.services

import android.content.Intent
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.helper.remote.RemoteLogicConfig
import com.word.file.manager.pdf.base.utils.isSamsungDevice

class CustomFCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val priority = message.priority
        if (RemoteMessage.PRIORITY_HIGH == priority) {
            if (!RemoteLogicConfig.fetchFeatureConfig().serviceKeepAlive) return
            if (isSamsungDevice()) return
            runCatching {
                ContextCompat.startForegroundService(app, Intent(app, CoreService::class.java))
            }
        }
    }

}