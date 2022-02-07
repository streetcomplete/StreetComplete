package de.westnordost.streetcomplete.quests.fire_hydrant_diameter

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestFireHydrantDiameterBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.FireHydrantDiameterMeasurementUnit.INCH
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.FireHydrantDiameterMeasurementUnit.MILLIMETER
import de.westnordost.streetcomplete.util.TextChangedWatcher

class AddFireHydrantDiameterForm : AbstractQuestFormAnswerFragment<FireHydrantDiameterAnswer>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_generic_answer_noSign) { confirmNoSign() }
    )

    override val contentLayoutResId = R.layout.quest_fire_hydrant_diameter
    private val binding by contentViewBinding(QuestFireHydrantDiameterBinding::bind)

    private val diameterValue get() = binding.diameterInput.text?.toString().orEmpty().trim().toIntOrNull() ?: 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.diameterInput.addTextChangedListener(TextChangedWatcher {
            checkIsFormComplete()
        })
    }

    override fun isFormComplete() = diameterValue > 0

    override fun onClickOk() {
        val diameter = if (countryInfo.countryCode == "GB" && diameterValue <= 25) {
            FireHydrantDiameter(diameterValue, INCH)
        } else {
            FireHydrantDiameter(diameterValue, MILLIMETER)
        }

        if (isUnusualDiameter(diameter))
            confirmUnusualInput { applyAnswer(diameter) }
        else
            applyAnswer(diameter)
    }

    private fun isUnusualDiameter(diameter: FireHydrantDiameter): Boolean {
        val value = diameter.value
        return when (diameter.unit) {
            MILLIMETER -> value > 600 || value < 50 || value % 5 != 0
            INCH -> value < 1 || value > 25
        }
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

    private fun confirmNoSign() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_fireHydrant_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoFireHydrantDiameterSign) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }
}
