package de.westnordost.streetcomplete.quests.fire_hydrant_diameter

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestFireHydrantDiameterBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.FireHydrantDiameterMeasurementUnit.INCH
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.FireHydrantDiameterMeasurementUnit.MILLIMETER
import de.westnordost.streetcomplete.util.ktx.intOrNull

class AddFireHydrantDiameterForm : AbstractOsmQuestForm<FireHydrantDiameterAnswer>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_generic_answer_noSign) { confirmNoSign() }
    )

    override val contentLayoutResId = R.layout.quest_fire_hydrant_diameter
    private val binding by contentViewBinding(QuestFireHydrantDiameterBinding::bind)

    private val diameterValue get() = binding.diameterInput.intOrNull ?: 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.diameterInput.doAfterTextChanged { checkIsFormComplete() }
    }

    override fun isFormComplete() = diameterValue > 0

    override fun onClickOk() {
        val diameter = if (countryInfo.countryCode == "GB" && diameterValue <= 25) {
            FireHydrantDiameter(diameterValue, INCH)
        } else {
            FireHydrantDiameter(diameterValue, MILLIMETER)
        }

        if (isUnusualDiameter(diameter)) {
            confirmUnusualInput(diameter.unit) { applyAnswer(diameter) }
        } else {
            applyAnswer(diameter)
        }
    }

    private fun isUnusualDiameter(diameter: FireHydrantDiameter): Boolean {
        val value = diameter.value
        return when (diameter.unit) {
            MILLIMETER -> value > 600 || value < 50 || value % 5 != 0
            INCH -> value < 1 || value > 25
        }
    }

    private fun confirmUnusualInput(
        unit: FireHydrantDiameterMeasurementUnit,
        onConfirmed: () -> Unit
    ) {
        val min = if (unit == MILLIMETER) 80 else 3
        val max = if (unit == MILLIMETER) 300 else 12
        val msg = getString(
            R.string.quest_fireHydrant_diameter_unusualInput_confirmation_description2,
            min, max
        )
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setMessage(msg)
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
