package com.word.file.manager.pdf.base.utils

import android.content.Context
import android.content.res.Configuration
import com.word.file.manager.pdf.base.helper.LocalPrefs
import java.util.Locale

fun Context.withSavedAppLocale(): Context {
    val languageCode = LocalPrefs.readDefaultLanguageCode(this)
        .ifBlank { Locale.getDefault().language }
    @Suppress("DEPRECATION")
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val configuration = Configuration(resources.configuration).apply {
        setLocale(locale)
        setLayoutDirection(locale)
    }
    return createConfigurationContext(configuration)
}
