package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao


class AddRoadSurface(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = """
        ways with highway ~ ${ROADS_WITH_SURFACES.joinToString("|")} and !surface
        and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val commitMessage = "Add road surfaces"
    override val icon = R.drawable.ic_quest_street_surface
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        val isSquare = tags["area"] == "yes"
        return if (hasName) {
            if (isSquare)
                R.string.quest_streetSurface_square_name_title
            else
                R.string.quest_streetSurface_name_title
        } else {
            if (isSquare)
                R.string.quest_streetSurface_square_title
            else
                R.string.quest_streetSurface_title
        }
    }

    override fun createForm() = AddRoadSurfaceForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("surface", answer)
    }

    companion object {
        // well, all roads have surfaces, what I mean is that not all ways with highway key are
        // "something with a surface"
        private val ROADS_WITH_SURFACES = arrayOf(
            // "trunk","trunk_link","motorway","motorway_link", // too much, motorways are almost by definition asphalt (or concrete)
            "primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
            "unclassified", "residential", "living_street", "pedestrian", "track", "road"
        )/*"service", */// this is too much, and the information value is very low
    }
}
