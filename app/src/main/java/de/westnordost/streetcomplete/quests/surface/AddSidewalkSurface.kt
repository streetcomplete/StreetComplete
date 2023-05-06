package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.sidewalk_surface.applyTo

class AddSidewalkSurface : OsmFilterQuestType<SidewalkSurfaceAnswer>() {

    // Only roads with 'complete' sidewalk tagging (at least one side has sidewalk, other side specified)
    override val elementFilter = """
        ways with
            highway ~ motorway|motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|service|living_street
            and area != yes
            and (
                sidewalk ~ both|left|right or
                sidewalk:both = yes or
                (sidewalk:left = yes and sidewalk:right ~ yes|no|separate) or
                (sidewalk:right = yes and sidewalk:left ~ yes|no|separate)
            )
            and (
                !sidewalk:both:surface and !sidewalk:left:surface and !sidewalk:right:surface
                or sidewalk:surface older today -8 years
            )
    """
    override val changesetComment = "Specify sidewalk surfaces"
    override val wikiLink = "Key:sidewalk"
    override val icon = R.drawable.ic_quest_sidewalk_surface
    override val achievements = listOf(PEDESTRIAN, WHEELCHAIR)
    override val defaultDisabledMessage = R.string.default_disabled_msg_difficult_and_time_consuming

    override fun getTitle(tags: Map<String, String>) = R.string.quest_sidewalk_surface_title

    override fun createForm() = AddSidewalkSurfaceForm()

    override fun applyAnswerTo(answer: SidewalkSurfaceAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is SidewalkIsDifferent -> {
                for (side in listOf(":left", ":right", ":both", "")) {
                    tags.remove("sidewalk$side:surface")
                    tags.remove("sidewalk$side:surface:note")
                    tags.remove("sidewalk$side:smoothness")
                    tags.remove("sidewalk$side")
                }
                tags.removeCheckDatesForKey("sidewalk")
                tags.removeCheckDatesForKey("sidewalk:surface")
                tags.removeCheckDatesForKey("sidewalk:smoothness")
            }
            is SidewalkSurface -> {
                answer.value.applyTo(tags)
            }
        }
    }
}
