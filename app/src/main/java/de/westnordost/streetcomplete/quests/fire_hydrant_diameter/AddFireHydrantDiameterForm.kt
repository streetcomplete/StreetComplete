package de.westnordost.streetcomplete.quests.fire_hydrant_diameter

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isGone
import androidx.core.widget.doAfterTextChanged
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.FireHydrantDiameterMeasurementUnit.INCH
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.FireHydrantDiameterMeasurementUnit.MILLIMETER
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.ktx.intOrNull
import de.westnordost.streetcomplete.util.mostCommonWithin
import org.koin.android.ext.android.inject

class AddFireHydrantDiameterForm : AbstractOsmQuestForm<FireHydrantDiameterAnswer>() {

    private val prefs: ObservableSettings by inject()

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_generic_answer_noSign) { confirmNoSign() }
    )

    override val contentLayoutResId get() = getHydrantDiameterSignLayoutResId(countryInfo.countryCode)
    private val diameterInput by lazy { requireView().findViewById<EditText>(R.id.diameterInput) }
    private val suggestionsButton by lazy { requireView().findViewById<View>(R.id.suggestionsButton) }

    private val diameterValue get() = diameterInput.intOrNull ?: 0

    private val lastPickedAnswers by lazy {
        favs.get()
            .mostCommonWithin(target = 5, historyCount = 15, first = 1)
            .sorted()
            .toList()
    }

    private lateinit var favs: LastPickedValuesStore<Int>

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            prefs,
            key = javaClass.simpleName,
            serialize = { it.toString() },
            deserialize = { it.toInt() }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateSuggestionsButtonVisibility()

        diameterInput.doAfterTextChanged {
            checkIsFormComplete()
            updateSuggestionsButtonVisibility()
        }

        suggestionsButton.setOnClickListener { showSuggestionsMenu() }
    }

    override fun isFormComplete() = diameterValue > 0

    override fun onClickOk() {
        val diameter = if (countryInfo.countryCode == "GB" && diameterValue <= 25) {
            FireHydrantDiameter(diameterValue, INCH)
        } else {
            FireHydrantDiameter(diameterValue, MILLIMETER)
        }

        if (isUnusualDiameter(diameter)) {
            confirmUnusualInput(diameter.unit) {
                favs.add(diameterValue)
                applyAnswer(diameter)
            }
        } else {
            favs.add(diameterValue)
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
            .setMessage(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoFireHydrantDiameterSign) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }

    private fun updateSuggestionsButtonVisibility() {
        suggestionsButton.isGone = lastPickedAnswers.isEmpty() || diameterInput.intOrNull != null
    }

    private fun showSuggestionsMenu() {
        val popup = PopupMenu(requireContext(), suggestionsButton)

        for (diameter in lastPickedAnswers) {
            popup.menu.add(diameter.toString())
        }

        popup.setOnMenuItemClickListener { item ->
            diameterInput.setText(item.title.toString())
            true
        }
        popup.show()
    }
}

private fun getHydrantDiameterSignLayoutResId(countryCode: String): Int = when (countryCode) {
    "DE", "BE", "LU", "AT" -> R.layout.quest_fire_hydrant_diameter_sign_de
    "HU" -> R.layout.quest_fire_hydrant_diameter_sign_hu
    "FI" -> R.layout.quest_fire_hydrant_diameter_sign_fi
    "NL" -> R.layout.quest_fire_hydrant_diameter_sign_nl
    "PL" -> R.layout.quest_fire_hydrant_diameter_sign_pl
    "GB", "IE" -> R.layout.quest_fire_hydrant_diameter_sign_uk
    else -> R.layout.quest_fire_hydrant_diameter_sign_generic
}
