package de.westnordost.streetcomplete.quests.bench_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN

class AddBenchMaterial : OsmFilterQuestType<BenchMaterial>() {

    override val elementFilter = """
        nodes, ways with
          (amenity = bench or leisure = picnic_table)
          and (!area or area = no)
          and !material
    """
    override val changesetComment = "Add material information to benches"
    override val wikiLink = "Tag:amenity=bench"
    override val icon = R.drawable.ic_quest_bench_material
    override val isDeleteElementEnabled = true
    override val questTypeAchievements = listOf(PEDESTRIAN, OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_benchMaterial_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = bench or leisure = picnic_table")

    override fun createForm() = AddBenchMaterialForm()

    override fun applyAnswerTo(answer: BenchMaterial, tags: Tags, timestampEdited: Long) {
        if (answer == BenchMaterial.PICNIC) {
            tags.remove("amenity")
            tags["leisure"] = "picnic_table"
        } else
            tags["material"] = answer.osmValue
    }
}
