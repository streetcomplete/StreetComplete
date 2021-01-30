package de.westnordost.streetcomplete.data

import android.util.Log
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.ktx.format
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis
import java.util.concurrent.FutureTask
import javax.inject.Inject

/** Initialize certain singleton classes used elsewhere throughout the app in the background */
class Preloader @Inject constructor(
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>,
    private val featuresDictionaryFuture: FutureTask<FeatureDictionary>
): CoroutineScope by CoroutineScope(Dispatchers.IO) {

    fun preload() {
        // country boundaries are necessary latest for when a quest is opened or on a download
        preloadCountryBoundaries()
        // names dictionary is necessary when displaying an element that has no name or
        // when downloading the place name quest (etc)
        preloadFeatureDictionary()
    }

    private fun preloadFeatureDictionary() = launch {
        val time = currentTimeMillis()
        featuresDictionaryFuture.run()
        val seconds = (currentTimeMillis() - time) / 1000.0
        Log.i(TAG, "Loaded features dictionary in ${seconds.format(1)}s")
    }

    private fun preloadCountryBoundaries() = launch {
        val time = currentTimeMillis()
        countryBoundariesFuture.run()
        val seconds = (currentTimeMillis() - time) / 1000.0
        Log.i(TAG, "Loaded country boundaries in ${seconds.format(1)}s")
    }

    companion object {
        private const val TAG = "Preloader"
    }
}
