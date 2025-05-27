package de.westnordost.streetcomplete.quests.bench_backrest

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.bench_backrest.BenchBackrestAnswer.NO
import de.westnordost.streetcomplete.quests.bench_backrest.BenchBackrestAnswer.PICNIC_TABLE
import de.westnordost.streetcomplete.quests.bench_backrest.BenchBackrestAnswer.YES

class AddBenchBackrest : OsmFilterQuestType<BenchBackrestAnswer>() {

    override val elementFilter = """
        nodes, ways with
          amenity = bench
          and (!area or area = no)
          and !backrest
          and !bench:type
          and (!seasonal or seasonal = no)
          and access !~ private|no
    """
    override val changesetComment = "Survey whether benches have backrests"
    override val wikiLink = "Tag:amenity=bench"
    override val icon = R.drawable.ic_quest_bench_poi
    override val isDeleteElementEnabled = true
    override val achievements = listOf(PEDESTRIAN, OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bench_backrest_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = bench or leisure = picnic_table")

    override fun createForm() = AddBenchBackrestForm()

    override fun applyAnswerTo(answer: BenchBackrestAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            PICNIC_TABLE -> {
                tags["leisure"] = "picnic_table"
                tags.remove("amenity")
            }
            YES -> tags["backrest"] = "yes"
            NO -> tags["backrest"] = "no"
        }
    }
}
