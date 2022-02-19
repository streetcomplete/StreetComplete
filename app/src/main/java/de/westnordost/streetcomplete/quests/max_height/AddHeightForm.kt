package de.westnordost.streetcomplete.quests.max_height

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestLengthBinding
import de.westnordost.streetcomplete.measure.ArSupportChecker
import de.westnordost.streetcomplete.measure.TakeMeasurementLauncher
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AddHeightForm : AbstractQuestFormAnswerFragment<Length>() {

    override val contentLayoutResId = R.layout.quest_length
    private val binding by contentViewBinding(QuestLengthBinding::bind)
    private val takeMeasurement = TakeMeasurementLauncher(this)
    private val checkArSupport: ArSupportChecker by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lengthInput.maxFeetDigits = 2
        binding.lengthInput.maxMeterDigitsBeforeDecimalPoint = 1
        binding.lengthInput.selectableUnits = countryInfo.lengthUnits
        binding.lengthInput.onInputChanged = { checkIsFormComplete() }
        binding.measureButton.isGone = !checkArSupport()
        binding.measureButton.setOnClickListener { lifecycleScope.launch { takeMeasurement() } }
    }

    private suspend fun takeMeasurement() {
        val lengthUnit = binding.lengthInput.unit ?: return
        val length = takeMeasurement(requireContext(), lengthUnit, true) ?: return
        binding.lengthInput.length = length
    }

    override fun onClickOk() {
        applyAnswer(binding.lengthInput.length!!)
    }

    override fun isFormComplete(): Boolean = binding.lengthInput.length != null
}
