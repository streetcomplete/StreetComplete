package de.westnordost.streetcomplete.quests.bench_backrest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddBenchBackrestForm : YesNoQuestAnswerFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        addOtherAnswer(R.string.quest_bench_answer_picnic_table) {
            val answer = Bundle()
            answer.putBoolean(PICNIC_TABLE, true)
            applyAnswer(answer)
        }
        return view
    }

    companion object {
        val PICNIC_TABLE = "picnic_table"
    }
}
