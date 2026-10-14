package de.westnordost.streetcomplete.screens

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.presets.EditTypePresetsSource
import de.westnordost.streetcomplete.data.urlconfig.UrlConfig
import de.westnordost.streetcomplete.data.urlconfig.UrlConfigController
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.util.ktx.launch
import de.westnordost.streetcomplete.util.ktx.toPosition
import de.westnordost.streetcomplete.util.parseGeoUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.maplibre.compose.camera.CameraPosition

/** Links the app was opened with. They are set by [MainNavHost] and shown on the main screen. */
@Stable
abstract class MainNavViewModel : ViewModel() {
    /** Whether the intro tutorial should be shown on start */
    abstract val shouldShowIntroTutorial: Boolean

    abstract fun setUri(uri: String)

    abstract val urlConfig: StateFlow<ShownUrlConfig?>
    abstract fun consumeUrlConfig()
    abstract fun applyUrlConfig(config: UrlConfig)
    abstract val geoUri: StateFlow<CameraPosition?>
    abstract fun consumeGeoUri()
}

data class ShownUrlConfig(val urlConfig: UrlConfig, val alreadyExists: Boolean)

@Stable
class MainNavViewModelImpl(
    private val urlConfigController: UrlConfigController,
    private val editTypePresetsSource: EditTypePresetsSource,
    private val prefs: Preferences,
) : MainNavViewModel() {

    override val shouldShowIntroTutorial: Boolean
        get() = !prefs.hasShownTutorial

    override fun setUri(uri: String) {
        launch {
            urlConfig.value = parseShownUrlConfig(uri)

            val geo = parseGeoUri(uri)
            if (geo != null) {
                val zoom = if (geo.zoom == null || geo.zoom < 14) 18.0 else geo.zoom
                val pos = LatLon(geo.latitude, geo.longitude)

                geoUri.value = CameraPosition(center = pos.toPosition(), bearing = 0.0, pitch = 0.0, zoom = zoom)
            }
        }
    }

    private suspend fun parseShownUrlConfig(uri: String): ShownUrlConfig? {
        val config = urlConfigController.parse(uri) ?: return null
        val alreadyExists = withContext(Dispatchers.IO) {
            config.presetName == null || editTypePresetsSource.getByName(config.presetName) != null
        }
        return ShownUrlConfig(urlConfig = config, alreadyExists = alreadyExists)
    }

    override val urlConfig = MutableStateFlow<ShownUrlConfig?>(null)

    override fun consumeUrlConfig() {
        urlConfig.value = null
    }

    override fun applyUrlConfig(config: UrlConfig) {
        launch(Dispatchers.IO) {
            urlConfigController.apply(config)
        }
    }

    override val geoUri = MutableStateFlow<CameraPosition?>(null)

    override fun consumeGeoUri() {
        geoUri.value = null
    }
}
