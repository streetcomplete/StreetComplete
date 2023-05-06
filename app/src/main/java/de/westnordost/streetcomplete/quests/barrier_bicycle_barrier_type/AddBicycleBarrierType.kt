package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BLIND
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags

class AddBicycleBarrierType : OsmFilterQuestType<BicycleBarrierTypeAnswer>() {

    override val elementFilter = "nodes with barrier = cycle_barrier and !cycle_barrier"
    override val changesetComment = "Specify cycle barrier types"
    override val wikiLink = "Key:cycle_barrier"
    override val icon = R.drawable.ic_quest_no_bicycles
    override val isDeleteElementEnabled = true

    override val achievements = listOf(BLIND, WHEELCHAIR, BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_barrier_type_title

    override fun createForm() = AddBicycleBarrierTypeForm()

    override fun applyAnswerTo(answer: BicycleBarrierTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is BicycleBarrierType -> tags["cycle_barrier"] = answer.osmValue
            BarrierTypeIsNotBicycleBarrier -> tags["barrier"] = "yes"
        }
    }
}
