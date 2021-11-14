package de.westnordost.streetcomplete.quests.level

import android.os.Bundle
import android.view.View

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestLevelBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.util.TextChangedWatcher

class AddLevelForm : AbstractQuestFormAnswerFragment<String>() {

    override val contentLayoutResId = R.layout.quest_level
    private val binding by contentViewBinding(QuestLevelBinding::bind)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //binding.levelButtonsContainer.addView()
    }

    override fun onClickOk() {
//        applyAnswer(level)
    }

    override fun isFormComplete() = false
}
