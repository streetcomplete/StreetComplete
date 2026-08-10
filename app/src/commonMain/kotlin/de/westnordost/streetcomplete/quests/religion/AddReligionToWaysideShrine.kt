package de.westnordost.streetcomplete.quests.religion

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddReligionToWaysideShrine : OsmFilterQuestType<Religion>() {

    override val elementFilter = """
        nodes, ways, relations with
          historic = wayside_shrine
          and !religion
          and access !~ private|no
    """
    override val changesetComment = "Specify religion for wayside shrines"
    override val wikiLink = "Key:religion"
    override val icon = Res.drawable.quest_religion
    override val title = Res.string.quest_religion_for_wayside_shrine_title
    override val achievements = listOf(OUTDOORS)

    @Composable
    override fun Form(on: (QuestAction<Religion>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddReligionForm(on, countryInfo)
    }

    override fun applyAnswerTo(answer: Religion, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["religion"] = answer.osmValue
    }
}
