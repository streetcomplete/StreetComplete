package de.westnordost.streetcomplete.quests.defibrillator

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.LIFESAVER
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddDefibrillatorLocation : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes with
        emergency = defibrillator
        and !location and !defibrillator:location
        and access !~ private|no"
    """
    override val changesetComment = "Specify defibrillator location"
    override val wikiLink = "Tag:emergency=defibrillator"
    override val icon = R.drawable.quest_defibrillator
    override val title = Res.string.quest_defibrillator_location
    override val achievements = listOf(LIFESAVER)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with emergency = defibrillator")

    @Composable
    override fun Form(onAnswer: (String) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddLocationDescriptionForm(onAnswer)
    }

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["defibrillator:location"] = answer
    }
}
