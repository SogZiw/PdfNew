package com.word.file.manager.pdf.base.utils

import android.os.Build

fun isAtLeastApi(versionCode: Int): Boolean {
    return Build.VERSION.SDK_INT >= versionCode
}

fun isAtLeastApi26(): Boolean = isAtLeastApi(Build.VERSION_CODES.O)
fun isAtLeastApi29(): Boolean = isAtLeastApi(Build.VERSION_CODES.Q)
fun isAtLeastApi30(): Boolean = isAtLeastApi(Build.VERSION_CODES.R)
fun isAtLeastApi31(): Boolean = isAtLeastApi(Build.VERSION_CODES.S)
fun isAtLeastApi33(): Boolean = isAtLeastApi(Build.VERSION_CODES.TIRAMISU)
fun isAtLeastApi34(): Boolean = isAtLeastApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
