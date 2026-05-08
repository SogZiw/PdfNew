package com.word.file.manager.pdf.base.helper

import android.content.Context
import android.content.Context.MODE_PRIVATE

object LocalPrefs {

    private const val STORE_NAME = "pdf_new_prefs"
    private const val KEY_DEFAULT_LANGUAGE_CODE = "defaultLanguageCode"

    private val store = PreferenceStore(STORE_NAME)

    var isFirstReqStorage by store.boolean(defaultValue = true)
    var defaultLanguageCode by store.string(key = KEY_DEFAULT_LANGUAGE_CODE, defaultValue = "")
    var userFirstCountryCode by store.string()
    var userDeviceId by store.string()
    var installReferrerUrl by store.string()
    var hasReqCloak by store.boolean(defaultValue = false)
    var userIsBlack by store.boolean(defaultValue = false)
    var hasCheckedLaunchConsent by store.boolean(defaultValue = false)
    var hasAskedNotificationPermission by store.boolean(defaultValue = false)
    var hasSeenIntroduce by store.boolean(defaultValue = false)
    var isPreviewUser by store.boolean(defaultValue = true)
    var totalRevenueFor001 by store.double()
    var hasSubscribeFMS by store.boolean(defaultValue = false)

    fun readDefaultLanguageCode(context: Context): String {
        return context.getSharedPreferences(STORE_NAME, MODE_PRIVATE)
            .getString(KEY_DEFAULT_LANGUAGE_CODE, "")
            .orEmpty()
    }
}
