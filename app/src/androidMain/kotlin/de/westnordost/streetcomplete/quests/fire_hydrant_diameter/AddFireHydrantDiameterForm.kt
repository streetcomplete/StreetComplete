package de.westnordost.streetcomplete.quests.fire_hydrant_diameter

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.FireHydrantDiameter.Unit.*
import de.westnordost.streetcomplete.ui.util.content

class AddFireHydrantDiameterForm : AbstractOsmQuestForm<FireHydrantDiameterAnswer>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_generic_answer_noSign) { confirmNoSign() }
    )

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    private lateinit var diameter: MutableState<Int?>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeViewBase.content { Surface {
            diameter = rememberSaveable { mutableStateOf(null) }

            HydrantDiameterForm(
                value = diameter.value,
                onValueChange = {
                    diameter.value = it
                    checkIsFormComplete()
                },
                countryCode = countryInfo.countryCode,
                modifier = Modifier.fillMaxWidth(),
            )
        } }
    }

    override fun isFormComplete() = diameter.value != null

    override fun onClickOk() {
        val value = diameter.value ?: 0
        val unit = if (countryInfo.countryCode == "GB" && value <= 25) Inch else Millimeter
        val diameter = FireHydrantDiameter(value, unit)

        if (isUnusualDiameter(diameter)) {
            confirmUnusualInput(diameter.unit) {
                applyAnswer(diameter)
            }
        } else {
            applyAnswer(diameter)
        }
    }

    private fun isUnusualDiameter(diameter: FireHydrantDiameter): Boolean {
        val value = diameter.value
        return when (diameter.unit) {
            Millimeter -> value !in 50..600 step 5
            Inch -> value !in 1..25
        }
    }

    private fun confirmUnusualInput(
        unit: FireHydrantDiameter.Unit,
        onConfirmed: () -> Unit
    ) {
        val range = when (unit) {
            Millimeter -> 80..300
            Inch -> 3..12
        }
        val msg = getString(
            R.string.quest_fireHydrant_diameter_unusualInput_confirmation_description2,
            range.start, range.endInclusive
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
            .setMessage(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(FireHydrantDiameterAnswer.NoSign) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }
}
