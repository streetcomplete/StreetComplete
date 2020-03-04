package de.westnordost.streetcomplete.quests.roof_shape

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao

class AddRoofShape(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = """
        ways, relations with roof:levels
        and roof:levels != 0 and !roof:shape and !3dr:type and !3dr:roof
    """
    override val commitMessage = "Add roof shapes"
    override val icon = R.drawable.ic_quest_roof_shape

    override fun getTitle(tags: Map<String, String>) = R.string.quest_roofShape_title

    override fun createForm() = AddRoofShapeForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("roof:shape", answer)
    }
}
