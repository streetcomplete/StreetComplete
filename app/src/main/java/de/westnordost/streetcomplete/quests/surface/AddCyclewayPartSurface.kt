package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi

class AddCyclewayPartSurface(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = """
        ways with
        (
          highway = cycleway 
          or (highway ~ path|footway and bicycle != no)
          or (highway = bridleway and bicycle ~ designated|yes)
        )
        and segregated = yes
        and !cycleway:surface and !surface:cycleway
    """
    override val commitMessage = "Add path surfaces"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_bicycleway_surface
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_cyclewayPartSurface_title

    override fun createForm() = AddPathSurfaceForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("cycleway:surface", answer)
    }
}
