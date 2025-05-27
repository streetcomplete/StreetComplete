package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestType

fun QuestType.getTitle(tags: Map<String, String>) =
    when (this) {
        is OsmElementQuestType<*> -> getTitle(tags)
        else -> title
    }
