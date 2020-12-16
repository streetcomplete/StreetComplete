package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder


class AddPitchSurface : OsmFilterQuestType<SurfaceAnswer>() {
        override val elementFilter = """
        ways with leisure=pitch
        and sport ~ soccer|tennis|basketball|equestrian|athletics|volleyball|skateboard|bmx|american_football|rugby_union
        and (access !~ private|no)
        and (
          !surface
          or surface older today -12 years
          or (
            surface ~ paved|unpaved
            and !surface:note
            and !note:surface
          )
        )
    """
        /* ~paved ways are less likely to change the surface type */

        override val commitMessage = "Add path surfaces"
        override val wikiLink = "Key:surface"
        override val icon = R.drawable.ic_quest_sport
        override val isSplitWayEnabled = true

        override fun getTitle(tags: Map<String, String>) = R.string.quest_pitchSurface_title



    override fun createForm() = AddPitchSurfaceForm()

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
}
