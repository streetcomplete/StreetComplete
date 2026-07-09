package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.QuestType
import kotlin.time.Clock

val LocalQuestType = compositionLocalOf<QuestType?> { null }

val LocalElement = compositionLocalOf<Element?> { null }

val LocalMapRotation = compositionLocalOf<Float> { 0f }
val LocalMapTilt = compositionLocalOf<Float> { 0f }

val LocalLastMapClick = compositionLocalOf<MapClick?> { null }

@Immutable
data class MapClick(
    val position: LatLon,
    val clickAreaSizeInMeters: Double,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)
