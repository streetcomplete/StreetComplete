package de.westnordost.streetcomplete.quests.backup_generator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddBackupGenerator : OsmFilterQuestType<Boolean>() {
    private val amenities = listOf(
        "bar", "cafe", "fast_food", "food_court", "pub", "restaurant", "cinema",
        "nightclub", "planetarium", "theatre", "internet_cafe", "car_wash", "vending_machine"
    )
    private val tourisms = listOf(
        "theme_park", "hotel", "hostel", "motel", "guest_house", "apartment", "camp_site",
        "museum", "gallery"
    )
    private val leisures = listOf(
        "adult_gaming_centre", "amusement_arcade", "bowling_alley", "escape_game",
        "miniature_golf", "sauna", "trampoline_park", "tanning_salon"
    )

    override val elementFilter = """
        nodes, ways with
        (
              (shop and shop !~ no|vacant)
              or amenity ~ ${amenities.joinToString("|")}
              or tourism ~ ${tourisms.joinToString("|")}
              or leisure ~ ${leisures.joinToString("|")}
        )
        and access !~ private|no
        and !backup_generator
    """
    override val changesetComment = "Survey availability of backup generator"
    override val wikiLink = "Key:backup_generator"
    override val icon = R.drawable.ic_quest_backup_generator
    override val isReplaceShopEnabled = true
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside_regional_warning

    override fun getTitle(tags: Map<String, String>) = R.string.quest_backupGenerator_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["backup_generator"] = answer.toYesNo()
    }
}
