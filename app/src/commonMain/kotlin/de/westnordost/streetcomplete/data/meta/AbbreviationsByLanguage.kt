package de.westnordost.streetcomplete.data.meta

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.ui.ktx.readYamlOrNull

/** (Road) name abbreviations and their expansions, accessible by language tag */
class AbbreviationsByLanguage(private val res: Res) {
    private val abbreviations = HashMap<String, Abbreviations?>()

    suspend operator fun get(languageTag: String): Abbreviations? =
        abbreviations.getOrPut(languageTag) {
            res.readYamlOrNull<Map<String, String>>("files/abbreviations/$languageTag.yml")
                ?.let { Abbreviations(it) }
        }
}
