package de.westnordost.streetcomplete.quests.width

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.databinding.QuestWidthBinding
import de.westnordost.streetcomplete.measure.ArSupportChecker
import de.westnordost.streetcomplete.measure.TakeMeasurementLauncher
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.toLengthUnit
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AddWidthForm : AbstractQuestFormAnswerFragment<Length>() {

    override val contentLayoutResId = R.layout.quest_width
    private val binding by contentViewBinding(QuestWidthBinding::bind)
    private val takeMeasurement = TakeMeasurementLauncher(this)
    private val checkArSupport: ArSupportChecker by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lengthInput.selectableUnits = countryInfo.lengthUnits.map { it.toLengthUnit() }
        binding.lengthInput.onInputChanged = { checkIsFormComplete() }
        binding.measureButton.isGone = !checkArSupport()
        binding.measureButton.setOnClickListener { lifecycleScope.launch { takeMeasurement() } }

        val isRoad = osmElement!!.tags["highway"] in ALL_ROADS
        val explanation = if (isRoad) getString(R.string.quest_road_width_explanation) else null

        binding.widthExplanationTextView.isGone = explanation == null
        binding.widthExplanationTextView.text = explanation
    }

    private suspend fun takeMeasurement() {
        val lengthUnit = binding.lengthInput.unit ?: return
        val length = takeMeasurement(requireContext(), lengthUnit, false) ?: return
        binding.lengthInput.length = length
    }

    override fun onClickOk() {
        applyAnswer(binding.lengthInput.length!!)
    }

    override fun isFormComplete(): Boolean =
        binding.lengthInput.length != null
}
