package de.westnordost.streetcomplete.ui.util.measure

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

abstract class ArMeasureViewModel : ViewModel() {

    /** Whether measuring with AR is supported at all */
    abstract fun isSupported(): Boolean

    /** Disable all quests that require AR */
    abstract fun disableArQuests()
}

class ArMeasureViewModelImpl(
    private val checkArSupport: ArSupportChecker,
    private val arQuestDisabler: ArQuestsDisabler,
) : ArMeasureViewModel() {

    override fun isSupported(): Boolean = checkArSupport()

    override fun disableArQuests() {
        launch(Dispatchers.IO) { arQuestDisabler.hideAllArQuests() }
    }
}
