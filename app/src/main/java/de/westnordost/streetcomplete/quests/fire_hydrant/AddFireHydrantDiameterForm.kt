package de.westnordost.streetcomplete.quests.fire_hydrant

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestFireHydrantDiameterBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.util.TextChangedWatcher

class AddFireHydrantDiameterForm : AbstractQuestFormAnswerFragment<Int>() {

    override val contentLayoutResId = R.layout.quest_fire_hydrant_diameter
    private val binding by contentViewBinding(QuestFireHydrantDiameterBinding::bind)

    private val diameter get() = binding.diameterInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.diameterInput.addTextChangedListener(TextChangedWatcher {
            checkIsFormComplete()
        })
    }

    override fun isFormComplete() = diameter.isNotEmpty() && diameter.toInt() > 0

    override fun onClickOk() {
        if (userSelectedUnusualDiameter())
            confirmUnusualInput { applyAnswer(diameter.toInt()) }
        else
            applyAnswer(diameter.toInt())

    }

    private fun userSelectedUnusualDiameter(): Boolean {
        val diameter = diameter.toInt()

        return diameter > 600 || diameter < 50 || diameter % 5 != 0
    }

    private fun confirmUnusualInput(onConfirmed: () -> Unit) {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setMessage(R.string.quest_fireHydrant_diameter_unusualInput_confirmation_description)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> onConfirmed() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }
}
