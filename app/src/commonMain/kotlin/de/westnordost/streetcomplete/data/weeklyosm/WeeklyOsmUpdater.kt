package de.westnordost.streetcomplete.data.weeklyosm

import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Updates the last publish date of weeklyOSM */
class WeeklyOsmUpdater(
    private val apiClient: WeeklyOsmApiClient,
    private val prefs: Preferences,
) {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    fun update() = coroutineScope.launch(Dispatchers.IO) {
        try {
            val publishDate = apiClient.getLastPublishDate()
            if (publishDate != null) {
                prefs.weeklyOsmLastPublishDate = publishDate.toLocalDate()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to download the last weeklyOSM publish date", e)
        }
    }

    companion object {
        private const val TAG = "WeeklyOsmUpdater"
    }
}
