package de.westnordost.streetcomplete.quests.defibrillator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.LIFESAVER
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddIsDefibrillatorIndoor : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes with
         emergency = defibrillator
         and access !~ private|no
         and !indoor and !location
    """
    override val changesetComment = "Determine whether defibrillators are inside buildings"
    override val wikiLink = "Key:indoor"
    override val icon = R.drawable.ic_quest_defibrillator
    override val achievements = listOf(LIFESAVER)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_is_defibrillator_inside_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with emergency = defibrillator")

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["indoor"] = answer.toYesNo()
    }
}
