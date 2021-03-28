package de.westnordost.streetcomplete.data.quest

import de.westnordost.osmapi.map.data.Element
import java.io.Serializable

sealed class QuestKey : Serializable

data class OsmNoteQuestKey(val noteId: Long) : QuestKey()

data class OsmQuestKey(
    val elementType: Element.Type,
    val elementId: Long,
    val questTypeName: String
) : QuestKey()
