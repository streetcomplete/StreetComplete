package de.westnordost.streetcomplete.quests.atm_operator

import androidx.compose.runtime.Composable
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
import de.westnordost.streetcomplete.ui.common.quest.NameWithSuggestionsQuestForm

class AddAtmOperator : OsmFilterQuestType<String>() {

    override val elementFilter = "nodes with amenity = atm and !operator and !name and !brand"
    override val changesetComment = "Specify ATM operator"
    override val wikiLink = "Tag:amenity=atm"
    override val icon = Res.drawable.quest_money
    override val title = Res.string.quest_atm_operator_title
    override val achievements = listOf(CITIZEN)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with amenity = atm")

    @Composable
    override fun Form(on: (QuestAction<String>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        NameWithSuggestionsQuestForm(
            suggestions = countryInfo.atmOperators,
            on = on
        )
    }

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["operator"] = answer
    }
}
