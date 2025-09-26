package de.westnordost.streetcomplete.quests.first_aid_kit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.LIFESAVER
import de.westnordost.streetcomplete.osm.Tags

class AddFirstAidKitLocation : OsmFilterQuestType<String>(), AndroidQuest {

    override val elementFilter = """
        nodes with
        emergency = first_aid_kit
        and !description and !first_aid_kit:description
        and access !~ private|no
    """
    override val changesetComment = "Specify first aid kit location"
    override val wikiLink = "Tag:emergency=first_aid_kit"
    //override val icon = R.drawable.ic_quest_defibrillator // Temporary icon
    override val icon = R.drawable.ic_quest_first_aid_kit
    override val isDeleteElementEnabled = false
    override val achievements = listOf(LIFESAVER)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_first_aid_kit_location

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with emergency = first_aid_kit")

    override fun createForm() = AddFirstAidKitLocationForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["first_aid_kit:description"] = answer
    }
}
