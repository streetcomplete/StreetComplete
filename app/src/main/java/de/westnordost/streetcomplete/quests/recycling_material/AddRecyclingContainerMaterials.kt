package de.westnordost.streetcomplete.quests.recycling_material

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.data.osm.tql.getQuestPrintStatement
import de.westnordost.streetcomplete.data.osm.tql.toGlobalOverpassBBox

class AddRecyclingContainerMaterials(
    private val overpassServer: OverpassMapDataAndGeometryDao)
    : OsmElementQuestType<List<String>> {

    override val commitMessage = "Add recycled materials to container"
    override val icon = R.drawable.ic_quest_recycling_materials

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpassServer.query(getOverpassQuery(bbox), handler)
    }

    private fun getOverpassQuery(bbox: BoundingBox) = """
        ${bbox.toGlobalOverpassBBox()}
        node[amenity = recycling][recycling_type = container] -> .all;
        node.all[~"^recycling:.*$" ~ ".*"] -> .known;
        (.all; - .known;);
        ${getQuestPrintStatement()}""".trimIndent()

    override fun isApplicableTo(element: Element): Boolean =
        element.tags?.let { tags ->
            tags["amenity"] == "recycling" &&
            tags["recycling_type"] == "container" &&
            tags.none { it.key.startsWith("recycling:") }
        } ?: false

    override fun getTitle(tags: Map<String, String>) = R.string.quest_recycling_materials_title

    override fun createForm() = AddRecyclingContainerMaterialsForm()

    override fun applyAnswerTo(answer: List<String>, changes: StringMapChangesBuilder) {
        for (accepted in answer) {
            changes.add(accepted, "yes")
        }
        // if the user chosse deliberately not "all plastic", be explicit about it
        if (answer.contains("recycling:plastic_packaging")) {
            changes.add("recycling:plastic", "no")
        }
        if (answer.contains("recycling:plastic_bottles")) {
            changes.add("recycling:plastic_packaging", "no")
            changes.add("recycling:plastic", "no")
        }
    }
}
