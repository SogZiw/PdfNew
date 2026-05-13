package com.word.file.manager.pdf.base.helper.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.helper.remote.RemoteLogicConfig
import com.word.file.manager.pdf.base.helper.services.CoreService
import com.word.file.manager.pdf.base.utils.isSamsungDevice

class ReSideReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (!RemoteLogicConfig.fetchFeatureConfig().serviceKeepAlive) return
        if (isSamsungDevice()) return
        runCatching {
            ContextCompat.startForegroundService(app, Intent(app, CoreService::class.java))
        }
    }

}