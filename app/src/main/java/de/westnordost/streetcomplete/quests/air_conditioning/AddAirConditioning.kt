package de.westnordost.streetcomplete.quests.air_conditioning

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddAirConditioning : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
          amenity ~ restaurant|bar|pub|cafe|library
          or tourism ~ apartment|hotel
        )
        and !air_conditioning
    """
    override val changesetComment = "Add air conditioning"
    override val wikiLink = "Key:air_conditioning"
    override val icon = R.drawable.ic_quest_snow_poi
    override val isReplaceShopEnabled = true
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside_regional_warning

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_airConditioning_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags["air_conditioning"] = answer.toYesNo()
    }
}
