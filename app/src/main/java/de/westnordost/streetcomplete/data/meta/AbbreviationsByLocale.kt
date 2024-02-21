package de.westnordost.streetcomplete.data.meta

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.getJsonObject
import java.util.Locale

class AbbreviationsByLocale(private val applicationContext: Context) {
    private val byLanguageAbbreviations = HashMap<String, Abbreviations>()

    operator fun get(locale: Locale): Abbreviations? {
        val code = locale.toString()
        if (!byLanguageAbbreviations.containsKey(code)) {
            byLanguageAbbreviations[code] = load(locale)
        }
        return byLanguageAbbreviations[code]
    }

    private fun load(locale: Locale): Abbreviations {
        val config = getResources(locale).getJsonObject<AbbreviationsFile>(R.raw.abbreviations)
        return Abbreviations(config.abbreviations, locale)
    }

    private fun getResources(locale: Locale): Resources {
        val configuration = Configuration(applicationContext.resources.configuration)
        configuration.setLocale(locale)
        return applicationContext.createConfigurationContext(configuration).resources
    }
}

private data class AbbreviationsFile(
    val fileFormatDescription: List<String>,
    val abbreviations: Map<String, String>,
)
