package de.westnordost.streetcomplete.quests.guidepost

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestGuidepostEleBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull

class AddGuidepostEleForm : AbstractOsmQuestForm<String>() {

    override val contentLayoutResId = R.layout.quest_guidepost_ele
    private val binding by contentViewBinding(QuestGuidepostEleBinding::bind)

    private val ele get() = binding.refInput.nonBlankTextOrNull

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.refInput.doAfterTextChanged { checkIsFormComplete() }
    }

    override fun onClickOk() {
        applyAnswer(ele!!)
    }


    override fun isFormComplete() = ele != null
}
