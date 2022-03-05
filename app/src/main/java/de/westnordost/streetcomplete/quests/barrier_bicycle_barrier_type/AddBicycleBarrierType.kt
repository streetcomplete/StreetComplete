package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BLIND
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type.BicycleBarrierType.NOT_CYCLE_BARRIER

class AddBicycleBarrierType : OsmFilterQuestType<BicycleBarrierType>() {

    override val elementFilter = "nodes with barrier = cycle_barrier and !cycle_barrier"
    override val changesetComment = "Add specific cycle barrier type"
    override val wikiLink = "Key:cycle_barrier"
    override val icon = R.drawable.ic_quest_no_bicycles
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = listOf(BLIND, WHEELCHAIR, BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_barrier_type_title

    override fun createForm() = AddBicycleBarrierTypeForm()

    override fun applyAnswerTo(answer: BicycleBarrierType, tags: Tags, timestampEdited: Long) {
        when (answer) {
            NOT_CYCLE_BARRIER -> {
                tags["barrier"] = "yes"
            }
            else -> tags["cycle_barrier"] = answer.osmValue
        }
    }
}
