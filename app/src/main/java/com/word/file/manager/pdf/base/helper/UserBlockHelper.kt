package com.word.file.manager.pdf.base.helper

import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.utils.isAppInstalled
import com.word.file.manager.pdf.isDebug

object UserBlockHelper {

    private val allowedReferrerMarks = mutableListOf("gad_source", "wcA", "wcB", "BwE", "3501424830017138")
    private val blockedReferrerMarks = mutableListOf("gclid=123456789")
    private var matchControlEnabled = true
    private val fakePkgMarks = mutableListOf(
        "com.just4funtools.fakegpslocationprofessional",
        "com.rosteam.gpsemulator",
        "com.lexa.fakegps",
        "com.blogspot.newapphorizons.fakegps",
        "com.hopefactory2021.fakegpslocation",
        "fake.gps.location.emulator",
        "com.incorporateapps.fakegps.fre",
        "com.mobile.fakelocation"
    )
    private val isAnyFakeInstalled by lazy { isInstallAnyPkgMarks() }
    var isUseFakeBlock = true

    fun updateAllowedMarks(open: Boolean, marks: List<String>?) {
        matchControlEnabled = open
        if (marks == null) return
        allowedReferrerMarks.replaceWith(marks)
    }

    fun updateBlockedMarks(marks: List<String>?) {
        if (marks == null) return
        blockedReferrerMarks.replaceWith(marks)
    }

    fun updateFakePkgMarks(open: Boolean, marks: List<String>?) {
        isUseFakeBlock = open
        if (marks == null) return
        fakePkgMarks.replaceWith(marks)
    }

    fun canShowExtra(enableTestAd: Boolean = true): Boolean {
        if (isDebug) return true
        if (isUseFakeBlock && isAnyFakeInstalled) {
            if (LocalPrefs.hasJudgedDeviceFake.not()) {
                LocalPrefs.hasJudgedDeviceFake = true
                EventCenter.logEvent("fake_g_user")
            }
            return false
        }
        return if (enableTestAd) {
            LocalPrefs.userIsBlack.not() && isReferrerAllowed() && LocalPrefs.isPreviewUser.not()
        } else {
            LocalPrefs.userIsBlack.not() && isReferrerAllowed()
        }
    }

    private fun isReferrerAllowed(referrer: String = LocalPrefs.installReferrerUrl): Boolean {
        if (referrer.isBlank()) return false
        if (blockedReferrerMarks.any { referrer.contains(it, ignoreCase = true) }) return false
        if (!matchControlEnabled) return true
        return allowedReferrerMarks.any { referrer.contains(it, ignoreCase = true) }
    }

    private fun isInstallAnyPkgMarks(): Boolean {
        return fakePkgMarks.any { app.isAppInstalled(it) }
    }

    private fun MutableList<String>.replaceWith(items: List<String>) {
        clear()
        addAll(items.filter { it.isNotBlank() })
    }
}
