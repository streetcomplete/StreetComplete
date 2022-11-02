package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags

class AddFootwayPartSurface : OsmFilterQuestType<SurfaceAnswer>() {

    override val elementFilter = """
        ways with (
          highway = footway
          or highway = path and foot != no
          or (highway ~ cycleway|bridleway and foot and foot != no)
        )
        and segregated = yes
        and !sidewalk
        and (
          !footway:surface
          or footway:surface older today -8 years
          or (
            footway:surface ~ paved|unpaved
            and !footway:surface:note
            and !note:footway:surface
          )
        )
        and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val changesetComment = "Add footway path surfaces"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_footway_surface
    override val achievements = listOf(PEDESTRIAN, WHEELCHAIR, OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_footwayPartSurface_title

    override fun createForm() = AddPathPartSurfaceForm()

    override fun applyAnswerTo(answer: SurfaceAnswer, tags: Tags, timestampEdited: Long) {
        answer.applyTo(tags, "footway:surface")
        if (tags["cycleway:surface"] != null && tags["footway:surface"] != null) {
            if (tags["footway:surface"] == tags["cycleway:surface"]) {
                tags["surface"] = tags["footway:surface"]!!
            } else {
                tags.remove("surface")
            }
        }
    }
}
