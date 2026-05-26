package de.westnordost.streetcomplete.quests.religion

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddReligionToPlaceOfWorship : OsmFilterQuestType<Religion>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
            amenity = place_of_worship
            or
            amenity = monastery
        )
        and !religion
    """
    override val changesetComment = "Specify religion for places of worship"
    override val wikiLink = "Key:religion"
    override val icon = Res.drawable.quest_religion
    override val title = Res.string.quest_religion_for_place_of_worship_title
    override val achievements = listOf(CITIZEN)

    @Composable
    override fun Form(onAnswer: (Religion) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddReligionForm(onAnswer, countryInfo)
    }

    override fun applyAnswerTo(answer: Religion, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["religion"] = answer.osmValue
    }
}
