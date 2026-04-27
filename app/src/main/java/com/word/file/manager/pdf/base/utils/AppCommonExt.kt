package com.word.file.manager.pdf.base.utils

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

fun Context.showMessageToast(msg: String, isShort: Boolean = true) {
    Toast.makeText(this, msg, if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
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
