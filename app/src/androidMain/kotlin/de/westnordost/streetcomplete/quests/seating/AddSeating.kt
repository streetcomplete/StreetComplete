package de.westnordost.streetcomplete.quests.seating

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo
import org.jetbrains.compose.resources.stringResource

class AddSeating : OsmFilterQuestType<Seating>() {

    override val elementFilter = """
        nodes, ways with
          (
            amenity ~ restaurant|cafe|fast_food|ice_cream|food_court|pub|bar
            or shop = bakery
          )
          and takeaway != only
          and (!outdoor_seating or !indoor_seating)
    """
    override val changesetComment = "Survey whether places have seating"
    override val wikiLink = "Key:outdoor_seating"
    override val icon = R.drawable.quest_seating
    override val title = Res.string.quest_seating_name_title
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_seasonal

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlaceOrDisusedPlace() }

    @Composable
    override fun Form(onAnswer: (Seating) -> Unit) {
        RadioGroupQuestForm(
            items = Seating.entries,
            itemContent = { Text(stringResource(it.text)) },
            onClickOk = onAnswer
        )
    }

    override fun applyAnswerTo(answer: Seating, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer == Seating.TAKEAWAY_ONLY) tags["takeaway"] = "only"
        tags["outdoor_seating"] = answer.hasOutdoorSeating.toYesNo()
        tags["indoor_seating"] = answer.hasIndoorSeating.toYesNo()
    }
}
