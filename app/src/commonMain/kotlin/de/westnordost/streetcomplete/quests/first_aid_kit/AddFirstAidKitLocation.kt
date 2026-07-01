package de.westnordost.streetcomplete.quests.first_aid_kit

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.LIFESAVER
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.defibrillator.AddLocationDescriptionForm
import de.westnordost.streetcomplete.resources.*

class AddFirstAidKitLocation : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes with
        emergency = first_aid_kit
        and !location and !first_aid_kit:location
        and access !~ private|no
    """
    override val changesetComment = "Specify first aid kit location"
    override val wikiLink = "Tag:emergency=first_aid_kit"
    override val icon = Res.drawable.quest_first_aid_kit
    override val title = Res.string.quest_first_aid_kit_location
    override val achievements = listOf(LIFESAVER)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with emergency = first_aid_kit")

    @Composable
    override fun Form(on: (QuestAction<String>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddLocationDescriptionForm(on)
    }

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["first_aid_kit:location"] = answer
    }
}
