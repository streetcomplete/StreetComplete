package de.westnordost.streetcomplete.quests.railway_platform_ref

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestRefBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull

class AddRailwayPlatformRefForm : AbstractOsmQuestForm<String>() {

    override val contentLayoutResId = R.layout.quest_ref
    private val binding by contentViewBinding(QuestRefBinding::bind)

    private val ref get() = binding.refInput.nonBlankTextOrNull

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.refInput.inputType = InputType.TYPE_CLASS_NUMBER
        binding.refInput.doAfterTextChanged { checkIsFormComplete() }
    }

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_railwayPlatformRef_abc) { binding.refInput.inputType = InputType.TYPE_CLASS_TEXT }
    )

    override fun onClickOk() {
        applyAnswer(ref!!)
    }

    override fun isFormComplete() = ref != null
}
