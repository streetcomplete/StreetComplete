package de.westnordost.streetcomplete.quests.camping

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
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

class AddCabins : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
          tourism ~ caravan_site|camp_site
          and !cabins
          and !backcountry
    """
    override val changesetComment = "Survey whether this site has cabins"
    override val wikiLink = "Key:cabins"
    override val icon = R.drawable.quest_cabin
    override val title = Res.string.quest_camp_cabin_title
    override val achievements = listOf(OUTDOORS)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with tourism ~ caravan_site|camp_site")

    @Composable
    override fun Form(onAnswer: (Boolean) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(onAnswer)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["cabins"] = answer.toYesNo()
    }
}
