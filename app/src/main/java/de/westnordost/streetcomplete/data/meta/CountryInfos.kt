package de.westnordost.streetcomplete.data.meta

import android.content.res.AssetManager
import com.esotericsoftware.yamlbeans.YamlReader
import de.westnordost.countryboundaries.CountryBoundaries
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.Future

class CountryInfos(
    private val assetManager: AssetManager,
    private val countryBoundaries: Future<CountryBoundaries>?,
) {
    private val countryInfoMap = HashMap<String, CountryInfo?>()
    private var defaultCountryInfo: CountryInfo? = null

    /** Get the info by location */
    fun get(longitude: Double, latitude: Double): CountryInfo {
        try {
            val countryCodesIso3166 = countryBoundaries?.get()?.getIds(longitude, latitude)
            return get(countryCodesIso3166 ?: emptyList())
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /** Get the info by a list of country codes sorted by size. I.e. DE-NI,DE,EU gets the info
     * for Niedersachsen in Germany and uses defaults from Germany and from the European Union */
    fun get(countryCodesIso3166: List<String>): CountryInfo {
        val result = CountryInfo()
        for (isoCode in countryCodesIso3166) {
            val countryInfo = get(isoCode)
            countryInfo?.let { result.complementWith(it) }
        }
        result.complementWith(default)
        return result
    }

    private operator fun get(countryCodeIso3166: String): CountryInfo? {
        if (!countryInfoMap.containsKey(countryCodeIso3166)) {
            val info = load(countryCodeIso3166)
            countryInfoMap[countryCodeIso3166] = info
        }
        return countryInfoMap[countryCodeIso3166]
    }

    private fun load(countryCodeIso3166: String): CountryInfo? {
        try {
            val countryInfosFiles = assetManager.list(BASEPATH)
            if (countryInfosFiles?.contains("$countryCodeIso3166.yml") == true) {
                return loadCountryInfo(countryCodeIso3166)
            }
            return null
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private val default: CountryInfo get() {
        try {
            if (defaultCountryInfo == null) {
                defaultCountryInfo = loadCountryInfo("default")
            }
            return defaultCountryInfo!!
        } catch (e: Exception) {
            // this should be in any case a programming error
            throw RuntimeException(e)
        }
    }

    @Throws(IOException::class)
    private fun loadCountryInfo(countryCodeIso3166: String): CountryInfo {
        val filename = "$countryCodeIso3166.yml"
        assetManager.open(BASEPATH + File.separator + filename).use { inputStream ->
            val reader = InputStreamReader(inputStream, "UTF-8")
            val yamlReader = YamlReader(reader)
            yamlReader.config.setPrivateFields(true)
            val result = yamlReader.read(CountryInfo::class.java)
            result.countryCode = countryCodeIso3166.split("-".toRegex()).toTypedArray()[0]
            return result
        }
    }

    /** Complement every declared field that is null with the field in [otherCountryInfo] */
    private fun CountryInfo.complementWith(otherCountryInfo: CountryInfo) {
        try {
            for (field in this.javaClass.declaredFields) {
                if (field[this] != null) continue

                val complementingField = otherCountryInfo.javaClass.getDeclaredField(field.name)
                field[this] = complementingField[otherCountryInfo]
            }
        } catch (e: Exception) { // IllegalAccessException, NoSuchFieldException
            // this should be in any case a programming error
            throw RuntimeException(e)
        }
    }

    companion object {
        private const val BASEPATH = "country_metadata"
    }
}
