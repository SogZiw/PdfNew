package com.word.file.manager.pdf.base.utils

import android.os.Build

fun isAtLeastAndroidVersion(versionCode: Int): Boolean {
    return Build.VERSION.SDK_INT >= versionCode
}

fun isAtLeastAndroid8(): Boolean = isAtLeastAndroidVersion(Build.VERSION_CODES.O)
fun isAtLeastAndroid10(): Boolean = isAtLeastAndroidVersion(Build.VERSION_CODES.Q)
fun isAtLeastAndroid11(): Boolean = isAtLeastAndroidVersion(Build.VERSION_CODES.R)
fun isAtLeastAndroid12(): Boolean = isAtLeastAndroidVersion(Build.VERSION_CODES.S)
fun isAtLeastAndroid13(): Boolean = isAtLeastAndroidVersion(Build.VERSION_CODES.TIRAMISU)
fun isAtLeastAndroid14(): Boolean = isAtLeastAndroidVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun isAtLeastAndroid15(): Boolean = isAtLeastAndroidVersion(Build.VERSION_CODES.VANILLA_ICE_CREAM)
