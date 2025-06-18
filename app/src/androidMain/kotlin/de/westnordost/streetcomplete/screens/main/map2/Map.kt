package de.westnordost.streetcomplete.screens.main.map2

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.screens.main.map2.style.MapStyleJawg
import dev.sargunv.maplibrecompose.compose.CameraState
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.StyleState
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.compose.rememberStyleState
import dev.sargunv.maplibrecompose.core.MapOptions
import dev.sargunv.maplibrecompose.core.OrnamentOptions

/**
 * A plain MapLibre Map with StreetComplete theme and localized names
 * */
@Composable
fun Map(
    modifier: Modifier = Modifier,
    cameraState: CameraState = rememberCameraState(),
    styleState: StyleState = rememberStyleState(),
) {
    // TODO maplibre-compose: The following gesture settings are missing (in MapOptions):
    //   flingThreshold = 250
    //   flingAnimationBaseTime = 500
    //   isDisableRotateWhenScaling = true
    //   // as workaround for https://github.com/maplibre/maplibre-native/issues/2792
    //   moveGestureDetector.moveThreshold = resources.dpToPx(5f)
    //   rotateGestureDetector.angleThreshold = 1.5f
    //   shoveGestureDetector.pixelDeltaThreshold = resources.dpToPx(8f)

    MaplibreMap(
        modifier = modifier,
        styleUri = Res.getUri("files/map_theme/empty.json"),
        zoomRange = 0f..22f,
        cameraState = cameraState,
        styleState = styleState,
        options = MapOptions(
            ornamentOptions = OrnamentOptions.AllDisabled
        )
    ) {
        val languages = listOf(Locale.current.language)

        MapStyleJawg(
            colors = if (isSystemInDarkTheme()) MapColors.Night else MapColors.Light,
            languages = languages,
        )
    }
}
