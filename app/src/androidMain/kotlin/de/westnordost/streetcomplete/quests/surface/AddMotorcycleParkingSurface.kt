package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.resources.*

class AddMotorcycleParkingSurface : OsmFilterQuestType(), AndroidQuest {
  override val elementFilter = """
    nodes, ways with amenity = motorcycle_parking
     and access !~ private|no
     and !surface
"""

    override val changesetComment = "Specify motorcycle parking surface"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.quest_motorcycle_parking
    override val title = Res.string.quest_surface_title
    override val achievements = listOf(OUTDOORS)

    override fun createForm() = AddMotorcycleParkingSurfaceForm()

    override fun applyAnswerTo(
        answer: Surface,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {
        answer.applyTo(tags)
    }
}
