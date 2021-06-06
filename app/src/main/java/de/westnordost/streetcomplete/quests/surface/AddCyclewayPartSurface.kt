package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder

class AddCyclewayPartSurface : OsmFilterQuestType<SurfaceAnswer>() {

    override val elementFilter = """
        ways with (
          highway = cycleway
          or (highway ~ path|footway and bicycle != no)
          or (highway = bridleway and bicycle ~ designated|yes)
        )
        and segregated = yes
        and !sidewalk
        and (
          !cycleway:surface
          or cycleway:surface older today -8 years
          or (
            cycleway:surface ~ paved|unpaved
            and !cycleway:surface:note
            and !note:cycleway:surface
          )
        )
    """
    override val commitMessage = "Add path surfaces"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_bicycleway_surface
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_cyclewayPartSurface_title

    override fun createForm() = AddPathSurfaceForm()

    override fun applyAnswerTo(answer: SurfaceAnswer, changes: StringMapChangesBuilder) {
        when (answer) {
            is SpecificSurfaceAnswer -> {
                changes.updateWithCheckDate("cycleway:surface", answer.value.osmValue)
                changes.deleteIfExists("cycleway:surface:note")
            }
            is GenericSurfaceAnswer -> {
                changes.updateWithCheckDate("cycleway:surface", answer.value.osmValue)
                changes.addOrModify("cycleway:surface:note", answer.note)
            }
        }
        changes.deleteIfExists("source:cycleway:surface")
    }
}
