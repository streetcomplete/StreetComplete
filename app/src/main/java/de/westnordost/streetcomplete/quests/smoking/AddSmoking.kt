package de.westnordost.streetcomplete.quests.smoking

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

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
    override val icon = R.drawable.ic_quest_smoking
    override val isReplaceShopEnabled = true
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside_regional_warning

    override fun getTitle(tags: Map<String, String>) = R.string.quest_smoking_title2

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = SmokingAllowedForm()

    override fun applyAnswerTo(answer: SmokingAllowed, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("smoking", answer.osmValue)
    }
}
