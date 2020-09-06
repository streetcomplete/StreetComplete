package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType


class DetailRoadSurface(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<DetailSurfaceAnswer>(o) {

    override val tagFilters = """
        ways with highway ~ ${ALL_ROADS.joinToString("|")}
        and (
                !surface
            or
                (
                surface ~ paved|unpaved
                and !surface:note
                and segregated != yes
                )
        )
        and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val commitMessage = "More detailed road surfaces"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_street_surface

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

    override fun createForm() = DetailRoadSurfaceForm()

    override val isSplitWayEnabled = true

    override fun applyAnswerTo(answer: DetailSurfaceAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is SurfaceAnswer -> {
                changes.addOrModify("surface", answer.value)
                changes.deleteIfExists("source:surface")
            }
            is DetailingWhyOnlyGeneric -> {
                changes.addOrModify("surface", answer.value)
                changes.add("surface:note", answer.note)
            }
        }
    }
}
