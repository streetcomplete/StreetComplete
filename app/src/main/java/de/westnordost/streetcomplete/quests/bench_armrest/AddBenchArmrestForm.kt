package de.westnordost.streetcomplete.quests.bench_armrest

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.bench_armrest.BenchArmrestAnswer.NO
import de.westnordost.streetcomplete.quests.bench_armrest.BenchArmrestAnswer.PICNIC_TABLE
import de.westnordost.streetcomplete.quests.bench_armrest.BenchArmrestAnswer.YES

class AddBenchArmrestForm : AbstractOsmQuestForm<BenchArmrestAnswer>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(NO) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(YES) }
    )

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_bench_answer_picnic_table) { applyAnswer(PICNIC_TABLE) }
    )
}
