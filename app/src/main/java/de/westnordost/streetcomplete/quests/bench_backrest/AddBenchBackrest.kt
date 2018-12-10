package de.westnordost.streetcomplete.quests.bench_backrest

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddBenchBackrest(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = "nodes with amenity=bench and !backrest"
    override val commitMessage = "Add backrest information to benches"
    override val icon = R.drawable.ic_quest_bench

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bench_backrest_title

    override fun createForm() = AddBenchBackrestForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val isPicnicTable = answer.getBoolean(AddBenchBackrestForm.PICNIC_TABLE)

        if (isPicnicTable) {
            changes.add("leisure", "picnic_table")
            changes.delete("amenity")
        } else {
            val yesno = if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) "yes" else "no"
            changes.add("backrest", yesno)
        }
    }
}
