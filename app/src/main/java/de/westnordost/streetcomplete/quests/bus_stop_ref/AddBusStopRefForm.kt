package de.westnordost.streetcomplete.quests.bus_stop_ref

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestRefBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull

class AddBusStopRefForm : AbstractOsmQuestForm<BusStopRefAnswer>() {

    override val contentLayoutResId = R.layout.quest_ref
    private val binding by contentViewBinding(QuestRefBinding::bind)

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_ref_answer_noRef) { confirmNoRef() }
    )

    private val ref get() = binding.refInput.nonBlankTextOrNull

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.refInput.doAfterTextChanged { checkIsFormComplete() }
    }

    override fun onClickOk() {
        applyAnswer(BusStopRef(ref!!))
    }

    private fun confirmNoRef() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoBusStopRef) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun isFormComplete() = ref != null
}
