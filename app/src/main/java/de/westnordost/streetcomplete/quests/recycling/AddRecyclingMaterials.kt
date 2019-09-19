package de.westnordost.streetcomplete.quests.leaf_detail

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.data.osm.tql.getQuestPrintStatement
import de.westnordost.streetcomplete.data.osm.tql.toGlobalOverpassBBox
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingMaterialsForm

class AddRecyclingMaterials(private val overpassServer: OverpassMapDataDao) : OsmElementQuestType<List<String>> {
    override val commitMessage = "Add recycled materials"
    override val icon = R.drawable.ic_quest_recycling

    override fun download(bbox: BoundingBox, handler: MapDataWithGeometryHandler): Boolean {
        return overpassServer.getAndHandleQuota(getOverpassQuery(bbox), handler)
    }

    private fun getOverpassQuery(bbox: BoundingBox): String {
        return bbox.toGlobalOverpassBBox() +
                "(\n" +
                "  node[amenity=recycling][~\"^recycling:.*\$\"~\".*\"];\n" +
                "\n" +
                ") -> .known_materials;\n" +
                "(\n" +
                "\tnode[amenity=recycling];\n" +
                ") -> .all_recycling;\n" +
                "(.all_recycling; - .known_materials;);" +
                getQuestPrintStatement()
    }
    override fun isApplicableTo(element: Element):Boolean? = null

    override fun getTitle(tags: Map<String, String>) = R.string.quest_recycling_materials_title

    override fun createForm() = AddRecyclingMaterialsForm()

    override fun applyAnswerTo(answer: List<String>, changes: StringMapChangesBuilder) {
        for (accepted in answer) {
            changes.add(accepted, "yes")
        }
    }
}
