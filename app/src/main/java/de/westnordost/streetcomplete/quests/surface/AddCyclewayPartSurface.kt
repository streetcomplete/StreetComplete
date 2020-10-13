package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddCyclewayPartSurface(o: OverpassMapDataAndGeometryApi, r: ResurveyIntervalsStore)
    : SimpleOverpassQuestType<DetailSurfaceAnswer>(o) {

    override val tagFilters = """
        ways with
        (
          highway = cycleway
          or (highway ~ path|footway and bicycle != no)
          or (highway = bridleway and bicycle ~ designated|yes)
        )
        and segregated = yes
        and (
            !cycleway:surface or
            cycleway:surface older today -${r * 8} years
            or
                (
                cycleway:surface ~ paved|unpaved
                and !cycleway:surface:note
                )
            )
    """
    override val commitMessage = "Add path surfaces"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_bicycleway_surface
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_cyclewayPartSurface_title

    override fun createForm() = AddPathSurfaceForm()

    override fun applyAnswerTo(answer: DetailSurfaceAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is SurfaceAnswer -> {
                changes.updateWithCheckDate("cycleway:surface", answer.value)
                changes.deleteIfExists("source:cycleway:surface")
            }
            is DetailingWhyOnlyGeneric -> {
                changes.updateWithCheckDate("cycleway:surface", answer.value)
                changes.add("cycleway:surface:note", answer.note)
            }
        }    }
}
