package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment

/** A quest type appears as a pin with an icon on the map and when opened, the quest type's
 *  question is displayed along with a UI to answer that quest.
 *
 *  How many quests of which types have been solved is persisted for the statistics and each quest
 *  type can contribute to unlocking new achievement levels of certain types.
 *
 *  Most QuestType inherit from [OsmElementQuestType][de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType] */
interface QuestType<T> {

    /** the icon resource id used to display this quest type on the map */
    val icon: Int

    /** the string resource id used to display the quest's question */
    val title: Int

    /** the string resource id that explains why this quest is disabled by default or zero if it is
     *  not disabled by default.
     *
     *  Quests that do not fully fulfill the [quest guidelines](https://github.com/streetcomplete/StreetComplete/blob/master/QUEST_GUIDELINES.md),
     *  (e.g. often the requirement that the information is publicly available from the outside),
     *  are disabled by default. */
    val defaultDisabledMessage: Int get() = 0

    /** returns the fragment in which the user can add the data */
    fun createForm(): AbstractQuestAnswerFragment<T>

    /** The quest type can clean it's metadata that is older than the given timestamp here, if any  */
    fun deleteMetadataOlderThan(timestamp: Long) {}

    /** towards which achievements ssolving a quest of this type should count */
    val questTypeAchievements: List<QuestTypeAchievement>
}
