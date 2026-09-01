package de.westnordost.streetcomplete.quests.vending_machine

import androidx.compose.runtime.Composable
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddVendingMachineType() : OsmFilterQuestType<List<Feature>>() {

    override val elementFilter = """
        nodes with
        (
          amenity=vending_machine
          and !vending
        )
    """
    override val changesetComment = "Survey what a vending machine sells"
    override val wikiLink = "Key:vending"
    override val icon = Res.drawable.quest_doctor_type
    override val title = Res.string.quest_vending_machine_type
    override val achievements = listOf(CITIZEN)

    @Composable
    override fun Form(on: (QuestAction<List<Feature>>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddVendingMachineTypeForm(on, element)
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity=vending_machine")

    override fun applyAnswerTo(answer: List<Feature>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["vending"] = answer.joinToString(";") { it.tags["vending"] ?: "" }
    }
}
