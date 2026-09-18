package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.DpOffset
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.QuestType
import org.jetbrains.compose.resources.DrawableResource
import kotlin.time.Clock

val LocalQuestType = compositionLocalOf<QuestType?> { null }

val LocalElement = compositionLocalOf<Element?> { null }

val LocalMapRotation = compositionLocalOf<Float> { 0f }
val LocalMapTilt = compositionLocalOf<Float> { 0f }
val LocalMapMetersPerDp = compositionLocalOf<Double> { 0.0 }

val LocalLastMapClick = compositionLocalOf<MapClick?> { null }

val LocalMapMarkersCallback = compositionLocalOf<((Iterable<Marker>) -> Unit)?> { null }

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
