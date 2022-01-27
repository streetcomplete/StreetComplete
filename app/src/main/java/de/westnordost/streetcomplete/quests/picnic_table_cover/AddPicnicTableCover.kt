package de.westnordost.streetcomplete.quests.picnic_table_cover

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddPicnicTableCover : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes with leisure = picnic_table
         and access !~ private|no
         and !covered
    """

    override val changesetComment = "Add picnic table cover"
    override val wikiLink = "Key:covered"
    override val icon = R.drawable.ic_quest_picnic_table_cover
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_picnicTableCover_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with leisure = picnic_table")

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags["covered"] = answer.toYesNo()
    }
}
