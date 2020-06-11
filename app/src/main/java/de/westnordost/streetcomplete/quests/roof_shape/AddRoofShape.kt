package de.westnordost.streetcomplete.quests.roof_shape

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi

class AddRoofShape(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = """
        ways, relations with roof:levels and
        !3dr:type and !3dr:roof and !roof:shape
    """
    override val commitMessage = "Add roof shapes"
    override val wikiLink = "Key:roof:shape"
    override val icon = R.drawable.ic_quest_roof_shape

    override fun getTitle(tags: Map<String, String>) =
        if (tags.containsKey("addr:housenumber"))
            R.string.quest_roofShape_address_title
        else
            R.string.quest_roofShape_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val addr = tags["addr:housenumber"]
        return if (addr != null) arrayOf(addr) else arrayOf()
    }

    override fun createForm() = AddRoofShapeForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("roof:shape", answer)
    }
}
