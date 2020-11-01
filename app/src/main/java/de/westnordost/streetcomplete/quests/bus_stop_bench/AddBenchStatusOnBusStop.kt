package de.westnordost.streetcomplete.quests.bus_stop_bench

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddBenchStatusOnBusStop : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes with
        (
          (public_transport = platform and ~bus|trolleybus|tram ~ yes)
          or
          (highway = bus_stop and public_transport != stop_position)
        )
        and physically_present != no and naptan:BusStopType != HAR
        and (!bench or bench older today -4 years)
    """

    override val commitMessage = "Add whether a bus stop has a bench"
    override val wikiLink = "Key:bench"
    override val icon = R.drawable.ic_quest_bench_public_transport

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        val isTram = tags["tram"] == "yes"
        return if (isTram) {
            if (hasName) R.string.quest_busStopBench_tram_name_title
            else         R.string.quest_busStopBench_tram_title
        } else {
            if (hasName) R.string.quest_busStopBench_name_title
            else         R.string.quest_busStopBench_title
        }
    }

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("bench", answer.toYesNo())
    }
}
