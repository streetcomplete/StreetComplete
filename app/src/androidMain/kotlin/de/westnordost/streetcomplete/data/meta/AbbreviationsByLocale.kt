package de.westnordost.streetcomplete.data.meta

import android.content.res.AssetManager
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import de.westnordost.streetcomplete.util.ktx.openOrNull

/** (Road) name abbreviations and their expansions, accessible by language tag */
class AbbreviationsByLocale(private val assetManager: AssetManager) {
    private val byLanguageAbbreviations = HashMap<String, Lazy<Abbreviations?>>()

    operator fun get(languageTag: String): Abbreviations? =
        byLanguageAbbreviations.getOrPut(languageTag) { lazy { load(languageTag) } }.value

    private fun load(languageTag: String): Abbreviations? =
        assetManager.openOrNull("abbreviations/$languageTag.yml")?.use {
            Abbreviations(Yaml.default.decodeFromStream(it))
        }
}
