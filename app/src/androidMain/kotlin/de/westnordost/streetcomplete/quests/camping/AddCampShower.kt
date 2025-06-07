package de.westnordost.streetcomplete.quests.camping

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddCampShower : OsmFilterQuestType<Boolean>(), AndroidQuest {

    /* We only resurvey shower = yes and shower = no, as it might have more detailed
     * values from other editors, and we don't want to damage them */
    override val elementFilter = """
        nodes, ways with
          tourism ~ camp_site|alpine_hut|wilderness_hut and (
            !shower
            or shower older today -4 years and shower ~ yes|no
          )
    """
    override val changesetComment = "Specify whether there are showers available"
    override val wikiLink = "Key:shower"
    override val icon = R.drawable.ic_quest_shower
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_camp_shower_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with tourism = camp_site")

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["shower"] = answer.toYesNo()
    }
}
