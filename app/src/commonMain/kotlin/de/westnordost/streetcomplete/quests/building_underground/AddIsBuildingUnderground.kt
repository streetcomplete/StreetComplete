package de.westnordost.streetcomplete.quests.building_underground

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*

class AddIsBuildingUnderground : OsmFilterQuestType<Boolean>() {

    override val elementFilter = "ways, relations with building and layer ~ -[0-9]+ and !location"
    override val changesetComment = "Determine whether buildings are fully underground"
    override val wikiLink = "Key:location"
    override val icon = Res.drawable.quest_building_underground
    override val title = Res.string.quest_building_underground_title
    override val achievements = listOf(BUILDING)

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(on)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["location"] = if (answer) "underground" else "surface"
    }
}
