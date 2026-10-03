package de.westnordost.streetcomplete.data.meta

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.ui.ktx.readYamlOrNull
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundaries
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.Volatile

class CountryInfos(private val res: Res) {
    private val yaml = Yaml(
        configuration = YamlConfiguration(
            strictMode = false, // ignore unknown properties
        )
    )

    private val countryInfosLock = reentrantLock()
    @Volatile
    private var countryInfos = emptyMap<String, IncompleteCountryInfo?>()
    private val default: IncompleteCountryInfo by lazy { load("default")!! }

    /** Get the info by a list of country codes sorted by size. E.g. DE-NI,DE gets the info
     *  for Lower Saxony in Germany and uses defaults from Germany */
    fun get(regionCode: List<String>): CountryInfo =
        CountryInfo(regionCode.firstOrNull(), regionCode.mapNotNull { get(it) } + default)

    private fun get(regionCode: String): IncompleteCountryInfo? {
        val cached = countryInfos
        if (cached.containsKey(regionCode)) {
            return cached[regionCode]
        }
        return countryInfosLock.withLock {
            val current = countryInfos
            if (current.containsKey(regionCode)) {
                current[regionCode]
            } else {
                load(regionCode).also {
                    countryInfos = current + (regionCode to it)
                }
            }
        }
    }

    private fun load(regionCode: String): IncompleteCountryInfo? =
        runBlocking {
            res.readYamlOrNull<IncompleteCountryInfo>("files/country_metadata/$regionCode.yml", yaml)
        }
}

fun CountryInfos.get(countryBoundaries: CountryBoundaries, position: LatLon): CountryInfo =
    get(countryBoundaries.getIds(position))
