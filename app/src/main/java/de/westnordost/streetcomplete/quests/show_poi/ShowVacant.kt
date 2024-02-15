package de.westnordost.streetcomplete.quests.show_poi

import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.KEYS_THAT_SHOULD_BE_REMOVED_WHEN_PLACE_IS_REPLACED
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.osm.removeCheckDates
import de.westnordost.streetcomplete.osm.updateCheckDate
import de.westnordost.streetcomplete.quests.getLabelOrElementSelectionDialog
import de.westnordost.streetcomplete.quests.getLabelSources
import de.westnordost.streetcomplete.quests.shop_type.IsShopVacant
import de.westnordost.streetcomplete.quests.shop_type.ShopType
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeAnswer
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeForm

class ShowVacant : OsmFilterQuestType<ShopTypeAnswer>() {
    override val elementFilter = """
        nodes, ways, relations with
        shop = vacant
        or disused:shop
        or disused:amenity
        or disused:office
    """
    override val changesetComment = "Adjust vacant places"
    override val wikiLink = "Key:disused:"
    override val icon = R.drawable.ic_quest_poi_vacant
    override val dotColor = "grey"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_vacant
    override val dotLabelSources = getLabelSources("label", this, prefs)

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_poi_vacant_title

    override fun createForm() = ShopTypeForm()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlace() }

    override fun applyAnswerTo(answer: ShopTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is IsShopVacant -> {
                tags.updateCheckDate()
            }
            is ShopType -> {
                tags.removeCheckDates()

                for (key in tags.keys) {
                    if (KEYS_THAT_SHOULD_BE_REMOVED_WHEN_PLACE_IS_REPLACED.any { it.matches(key) }) {
                        tags.remove(key)
                    }
                }

                for ((key, value) in answer.tags) {
                    tags[key] = value
                }
            }
        }
    }

    override fun getQuestSettingsDialog(context: Context) = getLabelOrElementSelectionDialog(context, this, prefs)
}
