package de.westnordost.streetcomplete.quests.max_height

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.quests.AbstractArMeasureQuestForm
import de.westnordost.streetcomplete.quests.LengthForm
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import de.westnordost.streetcomplete.ui.util.content
import org.koin.android.ext.android.inject

class AddMaxPhysicalHeightForm : AbstractArMeasureQuestForm<MaxPhysicalHeightAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    private val checkArSupport: ArSupportChecker by inject()
    private var isARMeasurement: Boolean = false
    private lateinit var length: MutableState<Length?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let { isARMeasurement = it.getBoolean(AR) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeViewBase.content { Surface {
            length = remember { mutableStateOf(null) }
            val arIsSupported = checkArSupport()

            LengthForm(
                length = length.value,
                onChange = {
                    isARMeasurement = false
                    length.value = it
                    checkIsFormComplete()
                },
                selectableUnits = countryInfo.lengthUnits,
                showMeasureButton = arIsSupported,
                onClickMeasure = { takeMeasurement(it, measureVertical = true) },
                modifier = Modifier.fillMaxWidth(),
            )
        } }
    }

    override fun onMeasured(length: Length) {
        isARMeasurement = true
        this.length.value = length
        checkIsFormComplete()
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
