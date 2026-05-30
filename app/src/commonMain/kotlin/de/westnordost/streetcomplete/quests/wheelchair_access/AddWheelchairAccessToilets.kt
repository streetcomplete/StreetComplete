package de.westnordost.streetcomplete.quests.wheelchair_access

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.*

class AddWheelchairAccessToilets : OsmFilterQuestType<WheelchairAccess>() {

    override val elementFilter = """
        nodes, ways with
         amenity = toilets
         and access !~ no|private
         and (
           !wheelchair
           or wheelchair != yes and wheelchair older today -4 years
           or wheelchair older today -8 years
         )
    """
    override val changesetComment = "Specify wheelchair accessibility of toilets"
    override val wikiLink = "Key:wheelchair"
    override val icon = Res.drawable.quest_toilets_wheelchair
    override val title = Res.string.quest_wheelchairAccess_outside_title
    override val achievements = listOf(WHEELCHAIR)
    override val hint = Res.string.quest_wheelchairAccess_description_toilets
    override val hintImages = listOf(Res.drawable.wheelchair_sign)

    @Composable
    override fun Form(onAnswer: (QuestAnswer<WheelchairAccess>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddWheelchairAccessForm(onAnswer)
    }

    override fun applyAnswerTo(answer: WheelchairAccess, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("wheelchair", answer.osmValue)
    }
}
