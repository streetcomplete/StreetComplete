package de.westnordost.streetcomplete.screens.settings

import android.content.SharedPreferences
import android.util.Log
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.Prefs.NavigationOrientation.MOVEMENT_DIRECTION
import de.westnordost.streetcomplete.Prefs.NavigationOrientation.COMPASS_DIRECTION
import de.westnordost.streetcomplete.Prefs.NavigationOrientation.valueOf

/** This class is just to access the user's preference about which navigation orioentation to use */
class NavigationOrientationUpdater(private val prefs: SharedPreferences) {
    /** Whether to use CompassDirection or MovementDirection for rotating screen if we're in isNavigationMode */
    var isCompassDirection: Boolean = false

    fun update() {
        val navDirection = navigationOrientation
        isCompassDirection = (navDirection == Prefs.NavigationOrientation.COMPASS_DIRECTION)

        Log.d("NavigationOrientationUpdater", "setting isCompassDirection to ${isCompassDirection} (from ${navDirection})")
    }

    private val navigationOrientation: Prefs.NavigationOrientation get() =
        valueOf(prefs.getString(Prefs.ORIENTATION_SELECT, "MOVEMENT_DIRECTION")!!)
}
