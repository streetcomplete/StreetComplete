package de.westnordost.streetcomplete.ui.util.measure

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class ArMeasureViewModel : ViewModel() {

    /** Whether measuring with AR is supported at all */
    abstract fun isSupported(): Boolean

    /** Last AR measurement result */
    abstract val measurementResult: StateFlow<ArMeasureResult?>

    /** Launch the measurement with AR */
    abstract fun measure(lengthUnit: LengthUnit, measureVertical: Boolean)

    /** Reset the last AR measurement result */
    abstract fun resetMeasurementResult()

    /** Disable all quests that require AR */
    abstract fun disableArQuests()
}

class ArMeasureViewModelImpl(
    private val launchMeasureApp: ArMeasureAppLauncher,
    private val checkArSupport: ArSupportChecker,
    private val arQuestDisabler: ArQuestsDisabler,
) : ArMeasureViewModel() {

    override val measurementResult = MutableStateFlow<ArMeasureResult?>(null)

    override fun isSupported(): Boolean = checkArSupport()

    override fun measure(lengthUnit: LengthUnit, measureVertical: Boolean) {
        launch {
            measurementResult.value = launchMeasureApp.measure(lengthUnit, measureVertical)
        }
    }

    override fun resetMeasurementResult() {
        measurementResult.value = null
    }

    override fun disableArQuests() {
        launch(Dispatchers.IO) { arQuestDisabler.hideAllArQuests() }
    }
}
