package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddFootwayPartSurface(o: OverpassMapDataDao) : SimpleOverpassQuestType<String>(o) {

	override val tagFilters = """
        ways with highway ~ path|footway|cycleway|bridleway and surface = paved and foot ~ designated|yes and segregated=yes
        and !footway:surface and !surface:footway
    """
	override val commitMessage = "Add path surfaces"
	override val icon = R.drawable.ic_quest_footway_surface
    override val isSplitWayEnabled = true

	override fun getTitle(tags: Map<String, String>) = R.string.quest_footwayPartSurface_title

	override fun createForm() = AddPathSurfaceForm()

	override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
		changes.add("footway:surface", answer)
	}
}
