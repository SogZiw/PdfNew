package com.word.file.manager.pdf.base.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
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

fun Activity.hideNavBars() {
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController ?: return
            controller.hide(WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }
}
