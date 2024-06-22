package de.westnordost.streetcomplete.quests.bench_armrest

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.bench_armrest.BenchArmrestAnswer.NO
import de.westnordost.streetcomplete.quests.bench_armrest.BenchArmrestAnswer.PICNIC_TABLE
import de.westnordost.streetcomplete.quests.bench_armrest.BenchArmrestAnswer.YES

class AddBenchArmrest : OsmFilterQuestType<BenchArmrestAnswer>() {

    override val elementFilter = """
        nodes, ways with
          amenity = bench
          and (!area or area = no)
          and !armrest
          and !bench:type
          and (!seasonal or seasonal = no)
          and access !~ private|no
    """
    override val changesetComment = "Survey whether benches have armrests"
    override val wikiLink = "Key:armrest"
    override val icon = R.drawable.ic_quest_bench_poi
    override val isDeleteElementEnabled = true
    override val achievements = listOf(PEDESTRIAN, OUTDOORS)

    override val hint = R.string.quest_bench_armrest_description

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bench_armrest_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = bench or leisure = picnic_table")

    override fun createForm() = AddBenchArmrestForm()

    override fun applyAnswerTo(answer: BenchArmrestAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            PICNIC_TABLE -> {
                tags["leisure"] = "picnic_table"
                tags.remove("amenity")
            }
            YES -> tags["armrest"] = "yes"
            NO -> tags["armrest"] = "no"
        }
    }
}
