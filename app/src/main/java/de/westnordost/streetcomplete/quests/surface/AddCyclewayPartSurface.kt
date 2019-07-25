package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddCyclewayPartSurface(o: OverpassMapDataDao) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = """
        ways with (
        (bicycle ~ designated|yes and segregated=yes and highway ~ path|footway|cycleway|bridleway and surface = paved)
        or
        (cycleway=track and highway ~ ${OsmTaggings.ALL_ROADS.joinToString("|")})
        )
        and !cycleway:surface and !surface:cycleway
    """
    override val commitMessage = "Add path surfaces"
    override val icon = R.drawable.ic_quest_way_surface

    override fun getTitle(tags: Map<String, String>) = R.string.quest_cyclewayPartSurface_title

    override fun createForm() = AddCyclewayPathSurfaceForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("cycleway:surface", answer)
    }
}
