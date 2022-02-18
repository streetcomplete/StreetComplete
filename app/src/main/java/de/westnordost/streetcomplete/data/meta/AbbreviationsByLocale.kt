package de.westnordost.streetcomplete.data.meta

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.charleskorn.kaml.Yaml
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.stringMapSerializer
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
        val inputStream = getResources(locale).openRawResource(R.raw.abbreviations)
        val config = Yaml.default.decodeFromStream(stringMapSerializer, inputStream)
        return Abbreviations(config, locale)
    }

    private fun getResources(locale: Locale): Resources {
        val configuration = Configuration(applicationContext.resources.configuration)
        configuration.setLocale(locale)
        return applicationContext.createConfigurationContext(configuration).resources
    }
}
