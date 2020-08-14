package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddPathSurface(o: OverpassMapDataAndGeometryApi, r: ResurveyIntervalsStore)
    : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = """
        ways with highway ~ path|footway|cycleway|bridleway|steps
        and segregated != yes
        and access !~ private|no
        and (!conveying or conveying = no) and (!indoor or indoor = no)
        and (
          !surface
          or surface ~ ${ANYTHING_UNPAVED.joinToString("|")} and surface older today -${r * 4} years
          or surface older today -${r * 8} years
        )
    """
    /* ~paved ways are less likely to change the surface type */

    override val commitMessage = "Add path surfaces"
    override val wikiLink = "Key:surface"
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
        changes.updateWithCheckDate("surface", answer)
    }
}
