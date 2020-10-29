package de.westnordost.streetcomplete.quests.bench_backrest

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.quests.bench_backrest.BenchBackrestAnswer.*

class AddBenchBackrest : OsmFilterQuestType<BenchBackrestAnswer>() {

    override val elementFilter = "nodes with amenity = bench and !backrest"
    override val commitMessage = "Add backrest information to benches"
    override val wikiLink = "Tag:amenity=bench"
    override val icon = R.drawable.ic_quest_bench_poi

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bench_backrest_title

    override fun createForm() = AddBenchBackrestForm()

    override fun applyAnswerTo(answer: BenchBackrestAnswer, changes: StringMapChangesBuilder) {
        when (answer) {
            PICNIC_TABLE -> {
                changes.add("leisure", "picnic_table")
                changes.delete("amenity")
            }
            YES -> changes.add("backrest", "yes")
            NO -> changes.add("backrest", "no")
        }
    }
}
