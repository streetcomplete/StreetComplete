package de.westnordost.streetcomplete.quests.lamp_mount

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags

class AddLampMount : OsmFilterQuestType<LampMountAnswer>() {

    override val elementFilter = """
        nodes with
          highway = street_lamp
          and !lamp_mount
          and !support
    """
    override val changesetComment = "Add lamp mount"
    override val defaultDisabledMessage = R.string.quest_lampMount_disabled_msg
    override val wikiLink = "Key:lamp_mount"
    override val icon = R.drawable.ic_quest_lamp_mount
    override val isReplacePlaceEnabled = true
    override val achievements = listOf(EditTypeAchievement.CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_lampMount_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with highway = street_lamp")

    override fun createForm() = AddLampMountForm()

    override fun applyAnswerTo(answer: LampMountAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is LampMount -> {
                tags["lamp_mount"] = answer.mount
            }
            is Support -> {
                tags["support"] = answer.mount
            }
        }
    }
}
