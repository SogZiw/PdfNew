package com.word.file.manager.pdf.base.helper.services

import android.content.Context
import android.content.Intent
import androidx.core.app.BaseJobService
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.utils.isSamsungDevice

class TaskJobIntentService : BaseJobService() {

    companion object {
        fun work(context: Context) {
            enqueueWork(context, TaskJobIntentService::class.java, 10000, Intent(context, TaskJobIntentService::class.java))
        }
    }

    override fun onHandleWork(intent: Intent) {
        if (isSamsungDevice()) return
        CoreService.showToolbar(app)
    }

}