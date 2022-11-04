package de.westnordost.streetcomplete.quests.bike_shop

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddBikeRepairAvailability : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with shop = bicycle
        and (
            !service:bicycle:repair
            or service:bicycle:repair older today -6 years
        )
        and access !~ private|no
    """

    override val changesetComment = "Specify whether bicycle shops offer repairs"
    override val wikiLink = "Key:service:bicycle:repair"
    override val icon = R.drawable.ic_quest_bicycle_repair
    override val achievements = listOf(BICYCLIST)
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_shop_repair_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("service:bicycle:repair", answer.toYesNo())
    }
}
