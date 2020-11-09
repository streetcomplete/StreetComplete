package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder

class AddRoadSurface : OsmFilterQuestType<SurfaceAnswer>() {

    override val elementFilter = """
        ways with highway ~ ${ROADS_WITH_SURFACES.joinToString("|")}
        and (
          !surface
          or surface ~ ${ANYTHING_UNPAVED.joinToString("|")} and surface older today -4 years
          or surface older today -12 years
          or (
            surface ~ paved|unpaved|cobblestone
            and !surface:note
            and !note:surface
          )
        )
        and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val commitMessage = "Add road surface info"
    override val wikiLink = "Key:surface"
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

    override fun applyAnswerTo(answer: SurfaceAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is SpecificSurfaceAnswer -> {
                changes.updateWithCheckDate("surface", answer.value)
                changes.deleteIfExists("surface:note")
            }
            is GenericSurfaceAnswer -> {
                changes.updateWithCheckDate("surface", answer.value)
                changes.addOrModify("surface:note", answer.note)
            }
        }
        changes.deleteIfExists("source:surface")
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
