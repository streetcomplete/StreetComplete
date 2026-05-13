package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BLIND
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddBicycleBarrierType : OsmFilterQuestType<BicycleBarrierTypeAnswer>() {

    override val elementFilter = "nodes with barrier = cycle_barrier and !cycle_barrier"
    override val changesetComment = "Specify cycle barrier types"
    override val wikiLink = "Key:cycle_barrier"
    override val icon = R.drawable.quest_no_bicycles
    override val title = Res.string.quest_bicycle_barrier_type_title
    override val achievements = listOf(BLIND, WHEELCHAIR, BICYCLIST)

    @Composable
    override fun Form(onAnswer: (BicycleBarrierTypeAnswer) -> Unit) {
        AddBicycleBarrierTypeForm(onAnswer)
    }

    override fun applyAnswerTo(answer: BicycleBarrierTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is BicycleBarrierType -> tags["cycle_barrier"] = answer.osmValue
            BarrierTypeIsNotBicycleBarrier -> tags["barrier"] = "yes"
        }
    }
}
