package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.WHEELCHAIR

class AddFootwayPartSurface : OsmFilterQuestType<SurfaceAnswer>() {

    override val elementFilter = """
        ways with (
          highway = footway
          or (highway ~ path|cycleway|bridleway and foot != no)
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
    """
    override val changesetComment = "Add path surfaces"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_footway_surface
    override val isSplitWayEnabled = true
    override val questTypeAchievements = listOf(PEDESTRIAN, WHEELCHAIR, OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_footwayPartSurface_title

    override fun createForm() = AddPathPartSurfaceForm()

    override fun applyAnswerTo(answer: SurfaceAnswer, tags: Tags, timestampEdited: Long) {
        answer.applyTo(tags, "footway:surface")
    }
}
