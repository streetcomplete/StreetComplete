package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao

class AddPathSurface(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = """
        ways with highway ~ path|footway|cycleway|bridleway|steps
        and !surface
        and segregated != yes
        and (!conveying or conveying = no) and (!indoor or indoor = no)
        and access !~ private|no
    """
    override val commitMessage = "Add path surfaces"
    override val icon = R.drawable.ic_quest_way_surface
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>) = when {
        tags["area"] == "yes"          -> R.string.quest_streetSurface_square_title
        tags["highway"] == "bridleway" -> R.string.quest_pathSurface_title_bridleway
        tags["highway"] == "steps"     -> R.string.quest_pathSurface_title_steps
        else                           -> R.string.quest_pathSurface_title
        // rest is rather similar, can be called simply "path"
    }

    override fun createForm() = AddPathSurfaceForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("surface", answer)
    }
}
