package com.word.file.manager.pdf.base.helper

object AppPrefs {

    private val store = PreferenceStore("pdf_new_prefs")

    var isFirstReqStorage by store.boolean(defaultValue = true)
}
