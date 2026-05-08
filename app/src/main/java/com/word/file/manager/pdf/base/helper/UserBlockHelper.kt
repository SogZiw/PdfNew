package com.word.file.manager.pdf.base.helper

import com.word.file.manager.pdf.isDebug

object UserBlockHelper {

    private val allowedReferrerMarks = mutableListOf("gad_source", "wcA", "wcB", "BwE", "3501424830017138")
    private val blockedReferrerMarks = mutableListOf("gclid=123456789")
    private var matchControlEnabled = true

    fun updateAllowedMarks(open: Boolean, marks: List<String>?) {
        matchControlEnabled = open
        if (marks == null) return
        allowedReferrerMarks.replaceWith(marks)
    }

    fun updateBlockedMarks(marks: List<String>?) {
        if (marks == null) return
        blockedReferrerMarks.replaceWith(marks)
    }

    fun canShowExtra(enableTestAd: Boolean = true): Boolean {
        if (isDebug) return true
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

    private fun MutableList<String>.replaceWith(items: List<String>) {
        clear()
        addAll(items.filter { it.isNotBlank() })
    }
}
