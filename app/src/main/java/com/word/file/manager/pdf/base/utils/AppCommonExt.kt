package com.word.file.manager.pdf.base.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.word.file.manager.pdf.isDebug
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

private const val DEFAULT_LOG_TAG = "AgilePDF"

fun Context.showMessageToast(msg: String, isShort: Boolean = true) {
    Toast.makeText(this, msg, if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
}

fun String.showLog(tag: String = DEFAULT_LOG_TAG) {
    if (isDebug) Log.d(tag, this)
}

fun buildPeriodicSignalFlow(
    intervalMs: Long,
    firstDelayMs: Long = 0L,
) = flow {
    delay(firstDelayMs)
    while (true) {
        emit(Unit)
        delay(intervalMs)
    }
}
