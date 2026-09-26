package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpOffset
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.QuestType
import org.jetbrains.compose.resources.DrawableResource
import org.maplibre.compose.overlay.MapOverlayScope
import kotlin.time.Clock

val LocalQuestType = compositionLocalOf<QuestType?> { null }

val LocalElement = compositionLocalOf<Element?> { null }

val LocalMapRotation = compositionLocalOf<Float> { 0f }
val LocalMapTilt = compositionLocalOf<Float> { 0f }
val LocalMapMetersPerDp = compositionLocalOf<Double> { 0.0 }

val LocalLastMapClick = compositionLocalOf<MapClick?> { null }

val LocalMapMarkersCallback = compositionLocalOf<((Iterable<Marker>) -> Unit)?> { null }

val LocalMapOverlayCallback = compositionLocalOf<((MapOverlayContent?) -> Unit)?> { null }

/** Content composed in the map's overlay, see [OnMap] */
typealias MapOverlayContent = @Composable MapOverlayScope.() -> Unit

/** Shows [content] on the map for as long as this composable is in the composition. It is composed
 *  in the map's overlay rather than in the form, so it stays put while the form animates, and it
 *  can be placed at map positions with [MapOverlayScope.placedAt]. */
@Composable
@NonSkippableComposable
fun OnMap(content: MapOverlayContent) {
    val setMapOverlay by rememberUpdatedState(LocalMapOverlayCallback.current)
    val currentContent by rememberUpdatedState(content)
    /* The map is composed before the form. When the form recomposes, the compiler keeps the
       identity of [content] and only updates what it captured, so the map is not told to compose
       it again. The runtime does invalidate the map's copy of a lambda when that lambda is updated,
       but drops the invalidation if the map inserted the copy in the same recomposition pass, and
       forgets the copy from then on. So the content is composed anew on the map on each
       recomposition of the form, which is what makes this function non-skippable. */
    var generation by remember { mutableIntStateOf(0) }
    SideEffect { generation++ }
    DisposableEffect(Unit) {
        setMapOverlay?.invoke { key(generation) { currentContent(this) } }
        onDispose { setMapOverlay?.invoke(null) }
    }
}

/** A click on the map: where on the map and where on the screen, relative to the map */
@Immutable
data class MapClick(
    val position: LatLon,
    val screenOffset: DpOffset,
    val clickAreaSizeInMeters: Double,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)

@Immutable
data class Marker(
    val geometry: ElementGeometry,
    val icon: DrawableResource? = null,
    val title: String? = null
)
