package de.westnordost.streetcomplete.data.meta

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.ui.ktx.readBytesOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString

class CountryInfos(private val res: Res) {
    private val yaml = Yaml(configuration = YamlConfiguration(
        strictMode = false, // ignore unknown properties
    ))

    private val countryInfoMap = HashMap<String, IncompleteCountryInfo?>()

    /** Get the info by a list of country codes sorted by size. I.e. DE-NI,DE,EU gets the info
     * for Niedersachsen in Germany and uses defaults from Germany and from the European Union */
    fun get(countryCodesIso3166: List<String>): CountryInfo = runBlocking {
        CountryInfo((countryCodesIso3166 + "default").mapNotNull { get(it) })
    }
    // TODO remove runBlocking and make suspend when consumers are straightforwardly able to use coroutines

    private suspend fun get(countryCodeIso3166: String): IncompleteCountryInfo? =
        countryInfoMap.getOrPut(countryCodeIso3166) { load(countryCodeIso3166) }

    private suspend fun load(countryCodeIso3166: String): IncompleteCountryInfo? {
        val path = "files/country_metadata/$countryCodeIso3166.yml"
        val bytes = res.readBytesOrNull(path) ?: return null
        val countryCode = countryCodeIso3166.split("-").first()
        return yaml.decodeFromString("countryCode: $countryCode\n" + bytes.decodeToString())
    }
}

fun CountryInfos.getByLocation(
    countryBoundaries: CountryBoundaries,
    longitude: Double,
    latitude: Double,
): CountryInfo =
    get(countryBoundaries.getIds(longitude, latitude))
