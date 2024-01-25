package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.quest.OsmQuestKey

/** Controller for managing which osm quests have been hidden by user interaction */
interface OsmQuestsHiddenController : OsmQuestsHiddenSource, HideOsmQuestController {

    /** Mark the quest as hidden by user interaction */
    override fun hide(key: OsmQuestKey)

    /** Un-hide the given quest. Returns whether it was hid before */
    fun unhide(key: OsmQuestKey): Boolean

    /** Un-hides all previously hidden quests by user interaction */
    fun unhideAll(): Int
}
