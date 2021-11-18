package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.OUTDOORS

class AddWhatIsOnBarrierAndPathIntersection: AddWhatIsOnBarrierLineAndWayIntersection() {

    override val pathsFilter by lazy {
        """
        ways with
          (highway ~ path|footway|steps|cycleway)
          and area != yes
          and (access !~ private|no or (foot and foot !~ private|no))
    """.toElementFilterExpression()
    }

    override val commitMessage = "Add what is on intersection of path and barrier"
    override fun getTitle(tags: Map<String, String>) = R.string.quest_barrier_path_intersection

    override val icon = R.drawable.ic_quest_barrier

    override val questTypeAchievements = listOf(PEDESTRIAN, WHEELCHAIR, OUTDOORS)
}
