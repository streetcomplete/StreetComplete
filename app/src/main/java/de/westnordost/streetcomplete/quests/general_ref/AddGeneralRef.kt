package de.westnordost.streetcomplete.quests.general_ref

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags

class AddGeneralRef : OsmFilterQuestType<GeneralRefAnswer>() {

    override val elementFilter = """
        nodes, ways with
          (
            (information = guidepost or guidepost) and guidepost != simple and hiking = yes
            or railway = subway_entrance and highway != elevator
            or building = service and power = substation
            or man_made = street_cabinet
            or highway = street_lamp
          )
          and !ref
          and noref != yes
          and ref:signed != no
          and !~"ref:.*"
    """
    override val changesetComment = "Specify refs"
    override val wikiLink = "Key:ref"
    override val icon = R.drawable.ic_quest_general_ref
    override val isDeleteElementEnabled = true
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_genericRef_title

    // substation buildings are not highlighted because those are usually far apart
    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("""
            nodes with
              information ~ guidepost|map
              or railway = subway_entrance
              or man_made = street_cabinet
              or highway = street_lamp
        """)
    override val highlightedElementsRadius: Double get() = 200.0

    override val defaultDisabledMessage: Int = R.string.quest_generalRef_disabled_msg

    override fun createForm() = AddGeneralRefForm()

    override fun applyAnswerTo(answer: GeneralRefAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NoVisibleGeneralRef -> tags["ref:signed"] = "no"
            is GeneralRef ->          tags["ref"] = answer.ref
        }
    }
}
