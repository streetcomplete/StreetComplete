package de.westnordost.streetcomplete.quests.air_conditioning

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.places.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddAirConditioning : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
        (
          amenity ~ restaurant|cafe|fast_food|ice_cream|food_court|pub|bar|library
          or tourism ~ apartment|hotel
        )
        and indoor_seating != no
        and takeaway != only
        and !air_conditioning
    """
    override val changesetComment = "Survey availability of air conditioning"
    override val wikiLink = "Key:air_conditioning"
    override val icon = Res.drawable.quest_snow_poi
    override val title = Res.string.quest_airConditioning_title
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside_regional_warning

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlaceOrDisusedPlace() }

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(on)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["air_conditioning"] = answer.toYesNo()
    }
}
