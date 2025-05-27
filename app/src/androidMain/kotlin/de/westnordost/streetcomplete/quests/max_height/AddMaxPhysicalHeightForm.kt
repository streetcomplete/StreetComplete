package de.westnordost.streetcomplete.quests.max_height

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestLengthBinding
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.quests.AbstractArMeasureQuestForm
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import de.westnordost.streetcomplete.view.controller.LengthInputViewController
import org.koin.android.ext.android.inject

class AddMaxPhysicalHeightForm : AbstractArMeasureQuestForm<MaxPhysicalHeightAnswer>() {

    override val contentLayoutResId = R.layout.quest_length
    private val binding by contentViewBinding(QuestLengthBinding::bind)
    private val checkArSupport: ArSupportChecker by inject()
    private var isARMeasurement: Boolean = false
    private lateinit var lengthInput: LengthInputViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let { isARMeasurement = it.getBoolean(AR) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lengthInput = binding.lengthInput.let {
            LengthInputViewController(it.unitSelect, it.metersContainer, it.metersInput, it.feetInchesContainer, it.feetInput, it.inchesInput)
        }
        lengthInput.unitSelectItemResId = R.layout.spinner_item_centered_large
        lengthInput.isCompactMode = true
        lengthInput.maxFeetDigits = 2
        lengthInput.maxMeterDigits = Pair(2, 2)
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
        takeMeasurement(lengthUnit, true)
    }

    override fun onMeasured(length: Length) {
        lengthInput.length = length
        isARMeasurement = true
    }

    override fun isFormComplete(): Boolean = lengthInput.length != null

    override fun onClickOk() {
        applyAnswer(MaxPhysicalHeightAnswer(lengthInput.length!!, isARMeasurement))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(AR, isARMeasurement)
    }

    companion object {
        private const val AR = "ar"
    }
}
