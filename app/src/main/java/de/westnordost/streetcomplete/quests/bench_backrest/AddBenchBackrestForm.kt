package de.westnordost.streetcomplete.quests.bench_backrest

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.bench_backrest.BenchBackrestAnswer.*

class AddBenchBackrestForm : AbstractQuestAnswerFragment<BenchBackrestAnswer>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(NO) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(YES) }
    )

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_bench_answer_picnic_table) { applyAnswer(PICNIC_TABLE) }
    )
}
