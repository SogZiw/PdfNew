package com.word.file.manager.pdf.base.data

data class LanguageItem(
    val displayName: String,
    val languageCode: String,
)

object LanguageCatalog {

    val supportedLanguages: List<LanguageItem> = listOf(
        LanguageItem("English", "en"),
        LanguageItem("繁體中文", "zh"),
        LanguageItem("日本語", "ja"),
        LanguageItem("한국어", "ko"),
        LanguageItem("Italiano", "it"),
        LanguageItem("Deutsch", "de"),
        LanguageItem("Français", "fr"),
        LanguageItem("Português", "pt"),
        LanguageItem("Español", "es"),
        LanguageItem("ภาษาไทย", "th"),
        LanguageItem("Bahasa Indonesia", "in"),
        LanguageItem("हिन्दी", "hi"),
        LanguageItem("Türkçe", "tr"),
        LanguageItem("العربية", "ar"),
    )
}
