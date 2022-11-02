package de.westnordost.streetcomplete.quests.bench_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags

class AddBenchMaterial : OsmFilterQuestType<BenchMaterial>() {

    override val elementFilter = """
        nodes, ways with
          (amenity = bench or leisure = picnic_table or amenity = lounger)
          and (!area or area = no)
          and !material
    """
    override val changesetComment = "Add material information to benches"
    override val wikiLink = "Tag:amenity=bench"
    override val icon = R.drawable.ic_quest_bench_material
    override val isDeleteElementEnabled = true
    override val achievements = listOf(EditTypeAchievement.PEDESTRIAN, EditTypeAchievement.OUTDOORS)
    override val defaultDisabledMessage: Int = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_benchMaterial_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = bench or leisure = picnic_table or amenity = lounger")

    override fun createForm() = AddBenchMaterialForm()

    override fun applyAnswerTo(answer: BenchMaterial, tags: Tags, timestampEdited: Long) {
        if (answer == BenchMaterial.PICNIC) {
            tags.remove("amenity")
            tags["leisure"] = "picnic_table"
        } else
            tags["material"] = answer.osmValue
    }
}
