package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.quest.OsmQuestKey

interface HideOsmQuestController {
    fun hide(key: OsmQuestKey)
    fun tempHide(key: OsmQuestKey)
}
