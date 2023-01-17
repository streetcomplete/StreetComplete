package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class QuestKey

@Serializable
@SerialName("osmnote")
data class OsmNoteQuestKey(val noteId: Long) : QuestKey()

@Serializable
@SerialName("osm")
data class OsmQuestKey(
    val elementType: ElementType,
    val elementId: Long,
    val questTypeName: String
) : QuestKey()

@Serializable
@SerialName("externalsource")
data class ExternalSourceQuestKey(val id: String, val source: String) : QuestKey()
