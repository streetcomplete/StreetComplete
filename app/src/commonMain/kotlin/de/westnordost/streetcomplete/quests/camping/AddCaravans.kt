package de.westnordost.streetcomplete.quests.camping

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddCaravans : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
          tourism = camp_site
          and !caravans
    """
    override val changesetComment = "Survey whether one can camp with caravans here"
    override val wikiLink = "Key:caravans"
    override val icon = Res.drawable.quest_caravan
    override val title = Res.string.quest_camp_caravan_title
    override val achievements = listOf(OUTDOORS)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with tourism ~ caravan_site|camp_site")

    @Composable
    override fun Form(onAnswer: (Boolean) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(onAnswer)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["caravans"] = answer.toYesNo()
    }
}
