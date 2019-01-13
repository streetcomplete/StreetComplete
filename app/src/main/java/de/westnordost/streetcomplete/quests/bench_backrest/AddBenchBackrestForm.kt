package de.westnordost.streetcomplete.quests.bench_backrest

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AYesNoQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.bench_backrest.BenchBackrestAnswer.*

class AddBenchBackrestForm : AYesNoQuestAnswerFragment<BenchBackrestAnswer>() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_bench_answer_picnic_table) { applyAnswer(PICNIC_TABLE) }
    )

    override fun onClick(answer: Boolean) {
        applyAnswer(if (answer) YES else NO)
    }
}
