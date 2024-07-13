package de.westnordost.streetcomplete.quests.trail_visibility

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddTrailVisibility : OsmFilterQuestType<TrailVisibility>() {

    override val elementFilter = """
        ways with
          highway ~ path|footway|cycleway|bridleway
          and !trail_visibility
          and ( access !~ no|private or foot ~ yes|permissive|designated or bicycle ~ yes|permissive|designated)
          and (sac_scale and sac_scale != hiking)
          and (!lit or lit = no)
          and surface ~ "ground|earth|dirt|soil|grass|sand|mud|ice|salt|snow|rock|stone"
    """
    override val changesetComment = "Specify Trail Visibility"
    override val wikiLink = "Key:trail_visibility"
    override val icon = R.drawable.ic_quest_trail_visibility
    override val defaultDisabledMessage = R.string.default_disabled_msg_trail_visibility

    override fun getTitle(tags: Map<String, String>) = R.string.quest_trail_visibility_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("ways with highway and trail_visibility")

    override fun createForm() = AddTrailVisibilityForm()

    override fun applyAnswerTo(answer: TrailVisibility, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["trail_visibility"] = answer.osmValue
    }
}
