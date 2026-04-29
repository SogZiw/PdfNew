package com.word.file.manager.pdf.base.helper

import android.content.Context
import android.content.Context.MODE_PRIVATE

object LocalPrefs {

    private const val STORE_NAME = "pdf_new_prefs"
    private const val KEY_DEFAULT_LANGUAGE_CODE = "defaultLanguageCode"

    private val store = PreferenceStore(STORE_NAME)

    var isFirstReqStorage by store.boolean(defaultValue = true)
    var defaultLanguageCode by store.string(key = KEY_DEFAULT_LANGUAGE_CODE, defaultValue = "")

    fun readDefaultLanguageCode(context: Context): String {
        return context.getSharedPreferences(STORE_NAME, MODE_PRIVATE)
            .getString(KEY_DEFAULT_LANGUAGE_CODE, "")
            .orEmpty()
    }
}
