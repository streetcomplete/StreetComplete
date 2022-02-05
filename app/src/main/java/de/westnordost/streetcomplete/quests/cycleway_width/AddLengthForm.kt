package de.westnordost.streetcomplete.quests.cycleway_width

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestLengthBinding
import de.westnordost.streetcomplete.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.measure.MeasureActivity
import de.westnordost.streetcomplete.measure.MeasureDisplayUnitFeetInch
import de.westnordost.streetcomplete.measure.MeasureDisplayUnitMeter
import de.westnordost.streetcomplete.measure.TakeMeasurementLauncher
import de.westnordost.streetcomplete.measure.isArSupported
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.LengthInFeetAndInches
import de.westnordost.streetcomplete.osm.LengthInMeters
import de.westnordost.streetcomplete.osm.LengthUnit
import de.westnordost.streetcomplete.osm.toLengthUnit
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import kotlinx.coroutines.launch

class AddLengthForm : AbstractQuestFormAnswerFragment<Length>() {

    override val contentLayoutResId = R.layout.quest_length
    private val binding by contentViewBinding(QuestLengthBinding::bind)
    private val takeMeasurement = TakeMeasurementLauncher(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lengthInput.selectableUnits = countryInfo.lengthUnits.map { it.toLengthUnit() }
        binding.measureButton.setOnClickListener { lifecycleScope.launch { takeMeasurement() } }

        viewLifecycleScope.launch {
            binding.measureButton.isGone = !isArSupported(requireContext())
        }
    }

    private suspend fun takeMeasurement() {
        val lengthUnit = binding.lengthInput.unit ?: return
        val length = takeMeasurement(requireContext(), lengthUnit, MeasureActivity.MeasureMode.HORIZONTAL) ?: return

        when (length) {
            is LengthInFeetAndInches -> {
                binding.lengthInput.feet = length.feet
                binding.lengthInput.inches = length.inches
            }
            is LengthInMeters -> {
                binding.lengthInput.meters = length.meters
            }
        }
    }

    override fun onClickOk() {
        applyAnswer(binding.lengthInput.length!!)
    }

    override fun isFormComplete(): Boolean =
        binding.lengthInput.length != null
}

suspend operator fun TakeMeasurementLauncher.invoke(
    context: Context,
    lengthUnit: LengthUnit,
    mode: MeasureActivity.MeasureMode? = null,
): Length? {
    val displayUnit = when (lengthUnit) {
        LengthUnit.METER -> MeasureDisplayUnitMeter(1)
        LengthUnit.FOOT_AND_INCH -> MeasureDisplayUnitFeetInch(4)
    }

    val meters = invoke(context, mode, displayUnit) ?: return null

    return when (displayUnit) {
        is MeasureDisplayUnitFeetInch -> {
            val (feet, inches) = displayUnit.getRounded(meters)
            LengthInFeetAndInches(feet, inches)
        }
        is MeasureDisplayUnitMeter -> {
            LengthInMeters(displayUnit.getRounded(meters))
        }
    }
}
