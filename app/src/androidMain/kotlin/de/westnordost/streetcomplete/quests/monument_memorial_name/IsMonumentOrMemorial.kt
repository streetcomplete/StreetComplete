package de.westnordost.streetcomplete.quests.monument_memorial_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags

class IsMonumentOrMemorial : OsmFilterQuestType<Boolean>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways, relations with
        historic = monument and !monument and !name and noname != yes and name:signed != no
        and historic older today -1 months
    """
    // Hide the element if its historic tag was changed in the last month.
    // This is here to prevent the quest from reappearing every time the element is identified as
    // a monument but still isn't name.

    override val changesetComment = "Specify whether this is a monument or a memorial"
    override val wikiLink = "Tag:historic=monument"
    override val icon = R.drawable.ic_quest_memorial
    override val achievements = listOf(BUILDING)

    override val hint = R.string.quest_is_monument_or_memorial_hint

    override fun getTitle(tags: Map<String, String>) = R.string.quest_is_monument_or_memorial_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways, relations with historic = monument and !monument and !name")

    override fun createForm() = IsMonumentOrMemorialForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        // if the node is identified as a memorial, change it's tag
        if (answer){
            tags["historic"] = "memorial"
        }else{
            // if the node is identified as a monument, this does nothing as it is already registered as a monument on OSM
            tags["historic"] = "monument"
        }
    }
}
