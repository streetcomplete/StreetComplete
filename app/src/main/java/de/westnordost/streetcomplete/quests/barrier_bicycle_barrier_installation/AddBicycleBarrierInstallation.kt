package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_installation

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestInstallation
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BLIND
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.LIFESAVER
import de.westnordost.streetcomplete.osm.Tags

class AddBicycleBarrierInstallation : OsmFilterQuestInstallation<BicycleBarrierInstallationAnswer>() {

    override val elementFilter = "nodes with barrier = cycle_barrier and cycle_barrier and !cycle_barrier=tilted and !cycle_barrier:installation"
    override val changesetComment = "Specify cycle barrier installation"
    override val wikiLink = "Key:cycle_barrier:installation"
    override val icon = R.drawable.ic_quest_no_bicycles
    override val isDeleteElementEnabled = true

    override val achievements = listOf(BLIND, WHEELCHAIR, BICYCLIST, LIFESAVER)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_barrier_installation_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with barrier=cycle_barrier")

    override fun createForm() = AddBicycleBarrierInstallationForm()

    override fun applyAnswerTo(answer: BicycleBarrierInstallationAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is BicycleBarrierInstallation -> tags["cycle_barrier:installation"] = answer.osmValue
            BarrierTypeIsNotBicycleBarrier -> tags["barrier"] = "yes"
        }
    }
}
