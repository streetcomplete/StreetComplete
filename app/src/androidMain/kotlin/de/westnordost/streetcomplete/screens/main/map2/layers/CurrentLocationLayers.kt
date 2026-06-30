package de.westnordost.streetcomplete.screens.main.map2.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.map_location_nyan
import de.westnordost.streetcomplete.resources.map_location_shadow
import de.westnordost.streetcomplete.resources.map_location_view_direction
import de.westnordost.streetcomplete.screens.main.map2.inMeters
import de.westnordost.streetcomplete.screens.main.map2.toGeometry
import de.westnordost.streetcomplete.ui.theme.Location
import de.westnordost.streetcomplete.util.ktx.isApril1st
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.CirclePitchAlignment
import org.maplibre.compose.expressions.value.IconPitchAlignment
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable

/** Displays the location + direction + accuracy marker on the map */
@Composable @MaplibreComposable
fun CurrentLocationLayers(
    location: Location,
    rotation: Float?
) {
    // TODO animate accuracy, position

    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(location.position.toGeometry())
    )

    CircleLayer(
        id = "accuracy",
        source = source,
        opacity = const(0.15f),
        color = const(Color.Location),
        radius = inMeters(
            width = location.accuracy,
            latitude = location.position.latitude
        ),
        strokeOpacity = const(0.5f),
        strokeColor = const(Color.Location),
        strokeWidth = const(1.dp),
        pitchAlignment = const(CirclePitchAlignment.Map),
    )
    if (rotation != null) {
        SymbolLayer(
            id = "direction",
            source = source,
            iconImage = image(painterResource(Res.drawable.map_location_view_direction)),
            iconAllowOverlap = const(true),
            iconIgnorePlacement = const(true),
            iconRotate = const(rotation),
            iconPitchAlignment = const(IconPitchAlignment.Map),
        )
    }
    SymbolLayer(
        id = "location-shadow",
        source = source,
        iconImage = image(painterResource(Res.drawable.map_location_shadow)),
        iconAllowOverlap = const(true),
        iconIgnorePlacement = const(true),
        iconPitchAlignment = const(IconPitchAlignment.Map),
    )

    // let's not check for the date on every recomposition :-)
    val isApril1st = remember { isApril1st() }
    if (!isApril1st) {
        CircleLayer(
            id = "location",
            source = source,
            color = const(Color.Location),
            radius = const(8.dp),
            strokeColor = const(Color.White),
            strokeWidth = const(2.dp),
            pitchAlignment = const(CirclePitchAlignment.Map)
        )
    } else {
        SymbolLayer(
            id = "location-nyan",
            source = source,
            iconImage = image(painterResource(Res.drawable.map_location_nyan)),
            iconSize = const(2f),
            iconAllowOverlap = const(true),
            iconIgnorePlacement = const(true),
            iconPitchAlignment = const(IconPitchAlignment.Viewport),
        )
    }
}
