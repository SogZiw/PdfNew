package com.word.file.manager.pdf.base.helper.remote

import android.content.Context
import com.google.android.gms.time.TrustedTime
import com.google.android.gms.time.TrustedTimeClient
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.helper.EventCenter
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.abs

object TrustedTimeHolder {

    private var isSendEvent = false

    @Volatile
    private var client: TrustedTimeClient? = null

    suspend fun getClient(context: Context): TrustedTimeClient? =
        client ?: suspendCancellableCoroutine { cont ->
            TrustedTime.createClient(context.applicationContext)
                .addOnCompleteListener { task ->
                    if (!cont.isActive) return@addOnCompleteListener
                    val result = if (task.isSuccessful) task.result else null
                    client = result
                    cont.resume(result)
                }
        }

    fun clear() {
        client?.dispose()
        client = null
    }

    private const val FIVE_MINUTES_MILLIS = 5 * 60 * 1000L

    fun isWithinFiveMinutesOfNow(time: Long): Boolean {
        val now = System.currentTimeMillis()
        return abs(time - now) <= FIVE_MINUTES_MILLIS
    }

    suspend fun fetchServiceTime(): Long? {
        if (isSendEvent.not()) EventCenter.logEvent("time_real_ask")
        val client = getClient(app) ?: return null
        val time = runCatching { client.computeCurrentUnixEpochMillis() }.getOrNull()
        if (time != null && time != 0L) {
            if (isSendEvent.not()) EventCenter.logEvent("time_real_suss")
            isSendEvent = true
        }
        return time
    }

}