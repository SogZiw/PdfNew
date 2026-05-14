package com.word.file.manager.pdf.modules.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.helper.notice.NoticeUtils
import com.word.file.manager.pdf.base.utils.isAtLeastAndroid11
import com.word.file.manager.pdf.base.utils.isAtLeastAndroid13

val legacyStoragePermissions: Array<String>
    get() = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

fun hasStorageAccessPermission(): Boolean {
    return if (isAtLeastAndroid11()) {
        Environment.isExternalStorageManager()
    } else {
        listOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        ).all { permission ->
            ContextCompat.checkSelfPermission(app, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}

fun hasOverlayPermission(): Boolean {
    return NoticeUtils.canDrawOverlaysByReflection(app)
}

fun hasPostNotificationPermission(): Boolean {
    return if (isAtLeastAndroid13()) {
        ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        NotificationManagerCompat.from(app).areNotificationsEnabled()
    }
}

fun shouldOpenAllFilesAccessPage(): Boolean = isAtLeastAndroid11()

fun shouldRequestLegacyStoragePermission(activity: Activity, isFirstRequest: Boolean): Boolean {
    return isFirstRequest || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
}

fun createAllFilesPermissionPageIntent(context: Context): Intent {
    return Intent(context, AllFilesPermissionActivity::class.java)
}

fun createOverlayPermissionPageIntent(context: Context): Intent {
    return Intent(context, ExtraPermissionActivity::class.java)
}

fun createAppDetailsSettingsIntent(context: Context): Intent {
    return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = "package:${context.packageName}".toUri()
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    }
}

fun createNotificationSettingsIntent(context: Context): Intent {
    return Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
}

fun Activity.openManageOverlayPermissionSettings(
    onFallbackFailure: (() -> Unit)? = null,
) {
    runCatching {
        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).also {
            it.data = "package:${packageName}".toUri()
            it.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        })
    }.onFailure {
        onFallbackFailure?.invoke()
    }
}

@SuppressLint("InlinedApi")
fun Activity.openManageAllFilesAccessSettings(
    onFallbackFailure: (() -> Unit)? = null,
) {
    runCatching {
        startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).also {
            it.data = "package:${packageName}".toUri()
            it.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        })
    }.onFailure {
        runCatching {
            startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).also {
                it.data = "package:${packageName}".toUri()
                it.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            })
        }.onFailure {
            onFallbackFailure?.invoke()
        }
    }
}
