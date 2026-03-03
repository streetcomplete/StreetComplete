package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.applyTo

class AddBeachSurface : OsmFilterQuestType<Surface>(), AndroidQuest {

    override val elementFilter = """
        ways, relations with
          natural = beach
          and !surface
    """

    override val changesetComment = "Specify beach surface"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.quest_beach
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_surface_title

    override fun createForm() = AddBeachSurfaceForm()

    override fun applyAnswerTo(
        answer: Surface,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {
        answer.applyTo(tags)
    }
}
