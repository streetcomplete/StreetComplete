package de.westnordost.streetcomplete.quests.bench_backrest

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddBenchBackrestForm : YesNoQuestAnswerFragment() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_bench_answer_picnic_table) { applyPicnicTableAnswer() }
    )

    private fun applyPicnicTableAnswer() {
        val answer = Bundle()
        answer.putBoolean(PICNIC_TABLE, true)
        applyAnswer(answer)
    }

    companion object {
        const val PICNIC_TABLE = "picnic_table"
    }
}
