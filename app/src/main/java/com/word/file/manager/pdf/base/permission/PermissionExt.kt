package com.word.file.manager.pdf.base.permission

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.utils.isAtLeastApi30

val legacyStoragePermissions: Array<String>
    get() = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

fun hasStorageAccessPermission(): Boolean {
    return if (isAtLeastApi30()) {
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

fun shouldOpenAllFilesAccessPage(): Boolean = isAtLeastApi30()

fun shouldRequestLegacyStoragePermission(activity: Activity, isFirstRequest: Boolean): Boolean {
    return isFirstRequest || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
}

fun createAllFilesPermissionPageIntent(context: Context): Intent {
    return Intent(context, AllFilesPermissionActivity::class.java)
}

fun createAppDetailsSettingsIntent(context: Context): Intent {
    return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = "package:${context.packageName}".toUri()
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
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
