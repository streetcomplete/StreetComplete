package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.osmapi.map.data.Element

data class OsmQuestKey(val elementType: Element.Type, val elementId: Long, val questTypeName: String)

val OsmQuest.key get() =
    OsmQuestKey(elementType, elementId, osmElementQuestType::class.simpleName!!)

val OsmQuestDaoEntry.key get() =
    OsmQuestKey(elementType, elementId, questTypeName)
