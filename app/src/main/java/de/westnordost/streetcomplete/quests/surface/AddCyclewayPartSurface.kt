package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags

class AddCyclewayPartSurface : OsmFilterQuestType<SurfaceAnswer>() {

    override val elementFilter = """
        ways with (
          highway = cycleway
          or (highway ~ path|footway and bicycle and bicycle != no)
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
        and (access !~ private|no or (foot and foot !~ private|no) or (bicycle and bicycle !~ private|no))
    """
    override val changesetComment = "Specify cycleway path surfaces"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_bicycleway_surface
    override val achievements = listOf(BICYCLIST, OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_cyclewayPartSurface_title

    override fun createForm() = AddPathPartSurfaceForm()

    override fun applyAnswerTo(answer: SurfaceAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags, "cycleway")
        answer.updateSegregatedFootAndCycleway(tags)
    }
}
