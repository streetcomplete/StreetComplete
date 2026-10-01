package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.osm.edits.EditType
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** A quest type appears as a pin with an icon on the map and when opened, the quest type's
 *  question is displayed along with a UI to answer that quest.
 *
 *  How many quests of which types have been solved is persisted for the statistics and each quest
 *  type can contribute to unlocking new achievement levels of certain types.
 *
 *  Most QuestType inherit from [OsmElementQuestType][de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType] */
interface QuestType : EditType {

    /** Hint text to be shown when the user taps on the ℹ️ button */
    val hint: StringResource? get() = null

    /** Hint pictures to be shown when the user taps on the ℹ️ button */
    val hintImages: List<DrawableResource> get() = emptyList()

    /** The quest type can clean its metadata that is older than the given timestamp here, if any  */
    fun deleteMetadataOlderThan(timestamp: Long) {}
}
