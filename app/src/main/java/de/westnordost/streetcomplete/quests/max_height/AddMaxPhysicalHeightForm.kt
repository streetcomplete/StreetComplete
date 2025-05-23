package de.westnordost.streetcomplete.quests.max_height

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestLengthBinding
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.quests.AbstractArMeasureQuestForm
import de.westnordost.streetcomplete.quests.LengthForm
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import de.westnordost.streetcomplete.ui.util.content
import org.koin.android.ext.android.inject

class AddMaxPhysicalHeightForm : AbstractArMeasureQuestForm<MaxPhysicalHeightAnswer>() {

    override val contentLayoutResId = R.layout.quest_length
    private val binding by contentViewBinding(QuestLengthBinding::bind)
    private val checkArSupport: ArSupportChecker by inject()
    private var isARMeasurement: Boolean = false
    private lateinit var length: MutableState<Length?>
    private lateinit var syncLength: MutableState<Boolean>
    private val countryLengthUnits = countryInfo.lengthUnits;
    private var currentUnit = countryLengthUnits[0]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let { isARMeasurement = it.getBoolean(AR) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.questLengthBase.content {
            length = rememberSaveable { mutableStateOf(null) }
            syncLength = rememberSaveable { mutableStateOf(false) }
            LengthForm(
                currentLength = length.value,
                syncLength = syncLength.value,
                onLengthChanged = {
                    syncLength.value = false
                    isARMeasurement = false
                    length.value = it
                    checkIsFormComplete()
                },
                maxFeetDigits = 3,
                maxMeterDigits = Pair(2, 2),
                selectableUnits = countryLengthUnits,
                onUnitChanged = { currentUnit = it },
                showMeasureButton = checkArSupport(),
                takeMeasurementClick = { takeMeasurement() },
                explanation = null
            )
        }
    }

    private fun takeMeasurement() {
        takeMeasurement(currentUnit, false)
    }

    override fun onMeasured(length: Length) {
        this.syncLength.value = true
        this.length.value = length
        isARMeasurement = true
    }

    override fun isFormComplete(): Boolean = length.value != null

    override fun onClickOk() {
        applyAnswer(MaxPhysicalHeightAnswer(length.value!!, isARMeasurement))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(AR, isARMeasurement)
    }

    companion object {
        private const val AR = "ar"
    }
}
