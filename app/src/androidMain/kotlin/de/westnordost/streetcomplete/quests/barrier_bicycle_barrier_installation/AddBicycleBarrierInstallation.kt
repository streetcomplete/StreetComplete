package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_installation

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.LIFESAVER
import de.westnordost.streetcomplete.osm.Tags

class AddBicycleBarrierInstallation : OsmFilterQuestType<BicycleBarrierInstallationAnswer>() {

    override val elementFilter = """
        nodes, ways with barrier = cycle_barrier
         and cycle_barrier
         and cycle_barrier != tilted
         and !cycle_barrier:installation
    """
    override val changesetComment = "Specify cycle barrier installation"
    override val wikiLink = "Key:cycle_barrier:installation"
    override val icon = R.drawable.ic_quest_no_bicycles
    override val isDeleteElementEnabled = true

    override val achievements = listOf(BICYCLIST, LIFESAVER)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_barrier_installation_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with barrier = cycle_barrier")

    override fun createForm() = AddBicycleBarrierInstallationForm()

    override fun applyAnswerTo(answer: BicycleBarrierInstallationAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is BicycleBarrierInstallation -> tags["cycle_barrier:installation"] = answer.osmValue
            BarrierTypeIsNotBicycleBarrier -> tags["barrier"] = "yes"
        }
    }
}
