package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.quest.QuestType

val LocalQuestType = compositionLocalOf<QuestType?> { null }

val LocalElement = compositionLocalOf<Element?> { null }

val LocalMapRotation = compositionLocalOf<Float> { 0f }
val LocalMapTilt = compositionLocalOf<Float> { 0f }
