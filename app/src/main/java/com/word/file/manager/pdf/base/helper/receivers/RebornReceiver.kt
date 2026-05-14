package com.word.file.manager.pdf.base.helper.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.word.file.manager.pdf.base.helper.notice.NoticeShower
import com.word.file.manager.pdf.base.helper.notice.NotificationScene

class RebornReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        runCatching { NoticeShower.showNotice(NotificationScene.ALARM) }
        NoticeShower.scheduleNextAlarm()
    }

}