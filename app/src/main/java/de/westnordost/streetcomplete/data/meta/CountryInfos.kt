package de.westnordost.streetcomplete.data.meta

import android.content.res.AssetManager
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import de.westnordost.countryboundaries.CountryBoundaries
import java.io.File
import java.io.SequenceInputStream

class CountryInfos(private val assetManager: AssetManager) {
    private val yaml = Yaml(configuration = YamlConfiguration(
        strictMode = false, // ignore unknown properties
    ))

    private val countryInfoMap = HashMap<String, IncompleteCountryInfo?>()
    private val defaultCountryInfo: IncompleteCountryInfo by lazy {
        loadCountryInfo("default")
    }

    /** Get the info by a list of country codes sorted by size. I.e. DE-NI,DE,EU gets the info
     * for Niedersachsen in Germany and uses defaults from Germany and from the European Union */
    fun get(countryCodesIso3166: List<String>): CountryInfo =
        CountryInfo(countryCodesIso3166.mapNotNull { get(it) } + defaultCountryInfo)

    private fun get(countryCodeIso3166: String): IncompleteCountryInfo? {
        if (!countryInfoMap.containsKey(countryCodeIso3166)) {
            val info = load(countryCodeIso3166)
            countryInfoMap[countryCodeIso3166] = info
        }
        return countryInfoMap[countryCodeIso3166]
    }

    private fun load(countryCodeIso3166: String): IncompleteCountryInfo? {
        val countryInfosFiles = assetManager.list(BASEPATH)
        if (countryInfosFiles?.contains("$countryCodeIso3166.yml") == true) {
            return loadCountryInfo(countryCodeIso3166)
        }
        return null
    }

    private fun loadCountryInfo(countryCodeIso3166: String): IncompleteCountryInfo {
        val filename = "$countryCodeIso3166.yml"
        assetManager.open(BASEPATH + File.separator + filename).use { inputStream ->
            val countryCode = countryCodeIso3166.split("-").first()
            val stream = SequenceInputStream("countryCode: $countryCode\n".byteInputStream(), inputStream)

            return yaml.decodeFromStream(stream)
        }
    }

    companion object {
        private const val BASEPATH = "country_metadata"
    }
}

fun CountryInfos.getByLocation(
    countryBoundaries: CountryBoundaries,
    longitude: Double,
    latitude: Double,
): CountryInfo =
    get(countryBoundaries.getIds(longitude, latitude) ?: emptyList())
