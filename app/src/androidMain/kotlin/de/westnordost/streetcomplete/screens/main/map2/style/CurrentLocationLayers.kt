package de.westnordost.streetcomplete.screens.main.map2.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.screens.main.map2.toPosition
import de.westnordost.streetcomplete.ui.theme.LocationDot
import de.westnordost.streetcomplete.util.ktx.isApril1st
import dev.sargunv.maplibrecompose.compose.MaplibreComposable
import dev.sargunv.maplibrecompose.compose.layer.CircleLayer
import dev.sargunv.maplibrecompose.compose.layer.SymbolLayer
import dev.sargunv.maplibrecompose.compose.source.rememberGeoJsonSource
import dev.sargunv.maplibrecompose.core.source.GeoJsonData
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.dp
import dev.sargunv.maplibrecompose.expressions.dsl.image
import dev.sargunv.maplibrecompose.expressions.value.CirclePitchAlignment
import dev.sargunv.maplibrecompose.expressions.value.IconPitchAlignment
import io.github.dellisd.spatialk.geojson.Point
import org.jetbrains.compose.resources.painterResource

/** Shows the location + direction + accuracy marker on the map */
@Composable @MaplibreComposable
fun CurrentLocationLayers(
    location: Location,
    rotation: Float?
) {
    // TODO animate accuracy, position

    val source = rememberGeoJsonSource(
        id = "location-source",
        data = GeoJsonData.Features(Point(location.position.toPosition()))
    )

    CircleLayer(
        id = "accuracy",
        source = source,
        opacity = const(0.15f),
        color = const(LocationDot),
        radius = inMeters(
            width = location.accuracy,
            latitude = location.position.latitude
        ).dp,
        strokeOpacity = const(0.5f),
        strokeColor = const(LocationDot),
        strokeWidth = const(1.dp),
        pitchAlignment = const(CirclePitchAlignment.Map),
    )
    if (rotation != null) {
        SymbolLayer(
            id = "direction",
            source = source,
            iconImage = image(painterResource(Res.drawable.location_view_direction)),
            iconAllowOverlap = const(true),
            iconIgnorePlacement = const(true),
            iconRotate = const(rotation),
            iconPitchAlignment = const(IconPitchAlignment.Map),
        )
    }
    SymbolLayer(
        id = "location-shadow",
        source = source,
        iconImage = image(painterResource(Res.drawable.location_shadow)),
        iconAllowOverlap = const(true),
        iconIgnorePlacement = const(true),
        iconPitchAlignment = const(IconPitchAlignment.Map),
    )
    CircleLayer(
        id = "location",
        source = source,
        color = const(LocationDot),
        radius = const(8.dp),
        strokeColor = const(Color.White),
        strokeWidth = const(2.dp),
        pitchAlignment = const(CirclePitchAlignment.Map)
    )
    // let's not check for the date on every recomposition :-)
    val isApril1st = remember { isApril1st() }
    if (isApril1st) {
        SymbolLayer(
            id = "location-nyan",
            source = source,
            iconImage = image(painterResource(Res.drawable.location_nyan)),
            iconSize = const(2f),
            iconAllowOverlap = const(true),
            iconIgnorePlacement = const(true),
            iconPitchAlignment = const(IconPitchAlignment.Viewport),
        )
    }
}
