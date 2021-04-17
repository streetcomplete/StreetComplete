package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import java.io.Serializable

sealed class QuestKey : Serializable

data class OsmNoteQuestKey(val noteId: Long) : QuestKey()

data class OsmQuestKey(
    val elementType: ElementType,
    val elementId: Long,
    val questTypeName: String
) : QuestKey()
