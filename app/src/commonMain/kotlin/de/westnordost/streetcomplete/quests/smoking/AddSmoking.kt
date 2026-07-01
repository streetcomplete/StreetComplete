package de.westnordost.streetcomplete.quests.smoking

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.places.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddSmoking : OsmFilterQuestType<SmokingAllowed>() {
    /*
        - some places are eligible as we assume they probably have seating (unless
          they are explicitly marked as having no seating at all):
          (like bar, cafe, restaurant...)
        - some places are eligible as their definition includes seating even if they
          don't have any other explicit tags (like outdoor_seating)
        - some places are eligible even if they don't have any seating (like nightclub)
        - some places are eligible only if they are explicitly marked to have seating
          as otherwise we assume they don't provide seating (like bakery, wine shop...)
     */
    override val elementFilter = """
         nodes, ways with
         (
             amenity ~ bar|cafe|biergarten|restaurant|food_court and (indoor_seating != no or outdoor_seating != no)
             or leisure = outdoor_seating
             or amenity ~ nightclub|stripclub|pub
             or (
                 (amenity ~ fast_food|ice_cream or shop ~ ice_cream|deli|bakery|coffee|tea|wine)
                 and (
                     (outdoor_seating and outdoor_seating != no)
                     or (indoor_seating and indoor_seating != no)
                 )
             )
         )
         and takeaway != only
         and (!smoking or smoking older today -8 years)
    """
    override val changesetComment = "Survey whether smoking is allowed or prohibited"
    override val wikiLink = "Key:smoking"
    override val icon = Res.drawable.quest_smoking
    override val title = Res.string.quest_smoking_title2
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside_regional_warning

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlaceOrDisusedPlace() }

    @Composable
    override fun Form(on: (QuestAction<SmokingAllowed>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        RadioGroupQuestForm(
            on = on,
            items = remember { SmokingAllowed.getSelectableValues(element.tags) },
            itemContent = { Text(stringResource(it.text)) },
        )
    }

    override fun applyAnswerTo(answer: SmokingAllowed, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("smoking", answer.osmValue)
    }
}
