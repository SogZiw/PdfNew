package com.word.file.manager.pdf.base.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
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

fun Int.dpToPx(activity: Activity): Int {
    return (this * activity.resources.displayMetrics.density).toInt()
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

fun isSamsungDevice() = Build.MANUFACTURER.equals("Samsung", ignoreCase = true)

fun isGoogleDevice() = Build.MANUFACTURER.equals("Google", ignoreCase = true)

fun Activity.hideNavBars() {
    runCatching {
        if (isAtLeastAndroid11()) {
            val controller = window.insetsController ?: return
            controller.hide(WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }
}

fun View.startButtonAnimation() {
    startAnimation(
        ScaleAnimation(
            1f,
            1.1f,
            1f,
            1.1f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
        ).apply {
            duration = 450L
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
            fillAfter = true
        }
    )
}
