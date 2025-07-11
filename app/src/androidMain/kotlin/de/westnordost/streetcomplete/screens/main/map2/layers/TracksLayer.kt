package de.westnordost.streetcomplete.screens.main.map2.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.map_track_nyan
import de.westnordost.streetcomplete.resources.map_track_nyan_record
import de.westnordost.streetcomplete.ui.theme.Location
import de.westnordost.streetcomplete.ui.theme.Recording
import de.westnordost.streetcomplete.util.ktx.isApril1st
import dev.sargunv.maplibrecompose.compose.MaplibreComposable
import dev.sargunv.maplibrecompose.compose.layer.LineLayer
import dev.sargunv.maplibrecompose.core.source.Source
import dev.sargunv.maplibrecompose.expressions.ast.Expression
import dev.sargunv.maplibrecompose.expressions.dsl.Feature
import dev.sargunv.maplibrecompose.expressions.dsl.condition
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.convertToBoolean
import dev.sargunv.maplibrecompose.expressions.dsl.image
import dev.sargunv.maplibrecompose.expressions.dsl.switch
import dev.sargunv.maplibrecompose.expressions.value.FloatValue
import dev.sargunv.maplibrecompose.expressions.value.LineCap
import org.jetbrains.compose.resources.painterResource

/** Displays a path(s) walked on the map */
@MaplibreComposable @Composable
fun TracksLayer(
    id: String,
    source: Source,
    opacity: Expression<FloatValue> = const(0.6f),
) {
    // let's not check for the date on every recomposition :-)
    val isApril1st = remember { isApril1st() }
    if (isApril1st) {
        TracksLayerApril1st(id, source, opacity)
    } else {
        TracksLayerDefault(id, source, opacity)
    }
}

@MaplibreComposable @Composable
private fun TracksLayerApril1st(
    id: String,
    source: Source,
    opacity: Expression<FloatValue>,
) {
    val recording = Feature.get("recording").convertToBoolean()

    LineLayer(
        id = id,
        source = source,
        opacity = opacity,
        width = const(26.dp),
        pattern = switch(
            condition(recording, image(painterResource(Res.drawable.map_track_nyan_record))),
            fallback = image(painterResource(Res.drawable.map_track_nyan))
        ),
    )
}

@MaplibreComposable @Composable
private fun TracksLayerDefault(
    id: String,
    source: Source,
    opacity: Expression<FloatValue>,
) {
    val recording = Feature.get("recording").convertToBoolean()

    LineLayer(
        id = id,
        source = source,
        opacity = opacity,
        cap = const(LineCap.Round),
        dasharray = const(listOf(0, 2)),
        width = const(6.dp),
        color = switch(
            condition(recording, const(Color.Recording)),
            fallback = const(Color.Location)
        ),
    )
}
