package de.westnordost.streetcomplete.data.meta

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.ui.ktx.readBytesOrNull
import de.westnordost.streetcomplete.util.ktx.getIds
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString

class CountryInfos(private val res: Res) {
    private val yaml = Yaml(
        configuration = YamlConfiguration(
            strictMode = false, // ignore unknown properties
        )
    )

    private val countryInfos = HashMap<String, IncompleteCountryInfo?>()
    private val default: IncompleteCountryInfo by lazy { load("default")!! }

    /** Get the info by a list of country codes sorted by size. E.g. DE-NI,DE gets the info
     *  for Lower Saxony in Germany and uses defaults from Germany */
    fun get(regionCode: List<String>): CountryInfo =
        CountryInfo(regionCode.mapNotNull { get(it) } + default)

    private fun get(regionCode: String): IncompleteCountryInfo? {
        if (!countryInfos.containsKey(regionCode)) {
            countryInfos[regionCode] = load(regionCode)
        }
        return countryInfos[regionCode]
    }

    private fun load(regionCode: String): IncompleteCountryInfo? {
        val bytes = runBlocking { res.readBytesOrNull("files/country_metadata/$regionCode.yml") }
        if (bytes == null) return null
        val countryCode = regionCode.split("-").first()
        val yml =
            "countryCode: $countryCode\n" +
            bytes.decodeToString()

        return yaml.decodeFromString(yml)
    }
}

fun CountryInfos.get(countryBoundaries: CountryBoundaries, position: LatLon): CountryInfo =
    get(countryBoundaries.getIds(position))
