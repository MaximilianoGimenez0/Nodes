package com.maxigimenez.nodes.core.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleHelper {

    fun setLocale(languageSetting: String) {
        val languageTag = when (languageSetting) {
            "Inglés" -> "en"
            "Portugués" -> "pt"
            else -> "es"
        }

        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageTag)

        if (AppCompatDelegate.getApplicationLocales() != appLocale) {
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}
