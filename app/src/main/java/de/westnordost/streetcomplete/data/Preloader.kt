package de.westnordost.streetcomplete.data

import android.util.Log
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.util.ktx.format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.FutureTask
import kotlin.system.measureTimeMillis

/** Initialize certain singleton classes used elsewhere throughout the app in the background */
class Preloader(
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>,
    private val featuresDictionaryFuture: FutureTask<FeatureDictionary>
) {

    suspend fun preload() {
        val execTimeMs = measureTimeMillis {
            coroutineScope {
                // country boundaries are necessary latest for when a quest is opened or on a download
                launch { preloadCountryBoundaries() }
                // names dictionary is necessary when displaying an element that has no name or
                // when downloading the place name quest (etc)
                launch { preloadFeatureDictionary() }
            }
        }
        Log.i(TAG, "Preloading data took ${(execTimeMs / 1000.0).format(1)}s")
    }

    private suspend fun preloadFeatureDictionary() = withContext(Dispatchers.IO) {
        val execTimeMs = measureTimeMillis { featuresDictionaryFuture.run() }
        Log.i(TAG, "Loaded features dictionary in ${(execTimeMs / 1000.0).format(1)}s")
    }

    private suspend fun preloadCountryBoundaries() = withContext(Dispatchers.IO) {
        val execTimeMs = measureTimeMillis { countryBoundariesFuture.run() }
        Log.i(TAG, "Loaded country boundaries in ${(execTimeMs / 1000.0).format(1)}s")
    }

    companion object {
        private const val TAG = "Preloader"
    }
}
