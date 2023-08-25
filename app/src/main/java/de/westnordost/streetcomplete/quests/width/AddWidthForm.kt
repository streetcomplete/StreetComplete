package de.westnordost.streetcomplete.quests.width

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestLengthBinding
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.hasDubiousRoadWidth
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import de.westnordost.streetcomplete.screens.measure.MeasureContract
import de.westnordost.streetcomplete.util.ktx.openUri
import de.westnordost.streetcomplete.view.controller.LengthInputViewController
import org.koin.android.ext.android.inject

class AddWidthForm : AbstractOsmQuestForm<WidthAnswer>() {

    override val contentLayoutResId = R.layout.quest_length
    private val binding by contentViewBinding(QuestLengthBinding::bind)
    private val launcher = registerForActivityResult(MeasureContract(), ::onMeasured)
    private val checkArSupport: ArSupportChecker by inject()
    private var isARMeasurement: Boolean = false
    private lateinit var lengthInput: LengthInputViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let { isARMeasurement = it.getBoolean(AR) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isRoad = element.tags["highway"] in ALL_ROADS
        val explanation = if (isRoad) getString(R.string.quest_road_width_explanation) else null
        binding.widthExplanationTextView.isGone = explanation == null
        binding.widthExplanationTextView.text = explanation

        lengthInput = binding.lengthInput.let {
            LengthInputViewController(it.unitSelect, it.metersContainer, it.metersInput, it.feetInchesContainer, it.feetInput, it.inchesInput)
        }
        lengthInput.unitSelectItemResId = R.layout.spinner_item_centered_large
        lengthInput.isCompactMode = true
        lengthInput.maxFeetDigits = if (isRoad) 3 else 2
        lengthInput.maxMeterDigits = Pair(if (isRoad) 2 else 1, 2)
        lengthInput.selectableUnits = countryInfo.lengthUnits
        lengthInput.onInputChanged = {
            isARMeasurement = false
            checkIsFormComplete()
        }
        binding.measureButton.isGone = !checkArSupport()
        binding.measureButton.setOnClickListener { takeMeasurement() }
    }

    private fun takeMeasurement() {
        val lengthUnit = lengthInput.unit ?: return
        try {
            launcher.launch(MeasureContract.Params(lengthUnit, false))
        } catch (e: ActivityNotFoundException) {
            context?.openUri("market://details?id=de.westnordost.streetmeasure")
        }
    }

    private fun onMeasured(length: Length?) {
        lengthInput.length = length
        isARMeasurement = true
    }

    override fun onClickOk() {
        val length = lengthInput.length!!
        val newTags = element.tags + ("width" to length.toMeters().toString())
        if (hasDubiousRoadWidth(newTags) != true) {
            applyAnswer(WidthAnswer(length, isARMeasurement))
        } else {
            confirmDubiousRoadWidth {
                applyAnswer(WidthAnswer(length, isARMeasurement))
            }
        }
    }

    private fun confirmDubiousRoadWidth(onConfirmed: () -> Unit) {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setMessage(R.string.quest_road_width_unusualInput_confirmation_description)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> onConfirmed() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }

    override fun isFormComplete(): Boolean = lengthInput.length != null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(AR, isARMeasurement)
    }

    companion object {
        private const val AR = "ar"
    }
}
