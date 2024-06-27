package de.westnordost.streetcomplete.data

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Initialize certain singleton classes used elsewhere throughout the app in the background */
class Preloader(
    private val countryBoundaries: Lazy<CountryBoundaries>,
    private val featuresDictionary: Lazy<FeatureDictionary>,
) {

    suspend fun preload() {
        val time = nowAsEpochMilliseconds()
        coroutineScope {
            // country boundaries are necessary latest for when a quest is opened or on a download
            launch { preloadCountryBoundaries() }
            // names dictionary is necessary when displaying an element that has no name or
            // when downloading the place name quest (etc)
            launch { preloadFeatureDictionary() }
        }

        Log.i(TAG, "Preloading data took ${((nowAsEpochMilliseconds() - time) / 1000.0).format(1)}s")
    }

    private suspend fun preloadFeatureDictionary() = withContext(Dispatchers.IO) {
        val time = nowAsEpochMilliseconds()
        featuresDictionary.value
        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
        Log.i(TAG, "Loaded features dictionary in ${seconds.format(1)}s")
    }

    private suspend fun preloadCountryBoundaries() = withContext(Dispatchers.IO) {
        val time = nowAsEpochMilliseconds()
        countryBoundaries.value
        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
        Log.i(TAG, "Loaded country boundaries in ${seconds.format(1)}s")
    }

    companion object {
        private const val TAG = "Preloader"
    }
}
