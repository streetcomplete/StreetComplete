package de.westnordost.streetcomplete.quests.width

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.hasDubiousRoadWidth
import de.westnordost.streetcomplete.quests.AbstractArMeasureQuestForm
import de.westnordost.streetcomplete.quests.LengthForm
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.koin.android.ext.android.inject

class AddWidthForm : AbstractArMeasureQuestForm<WidthAnswer>() {

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

        val isRoad = element.tags["highway"] in ALL_ROADS
        val arIsSupported = checkArSupport()

        binding.composeViewBase.content { Surface {
            length = rememberSerializable { mutableStateOf(null) }
            Column(Modifier.fillMaxWidth()) {
                if(isRoad) {
                    Text(stringResource(R.string.quest_road_width_explanation))
                }

                LengthForm(
                    length = length.value,
                    onChange = {
                        isARMeasurement = false
                        length.value = it
                        checkIsFormComplete()
                    },
                    selectableUnits = countryInfo.lengthUnits,
                    showMeasureButton = arIsSupported,
                    onClickMeasure = { takeMeasurement(it, measureVertical = false) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } }
    }

    override fun onMeasured(length: Length) {
        isARMeasurement = true
        this.length.value = length
        checkIsFormComplete()
    }

    override fun onClickOk() {
        val length = length.value!!
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

    override fun isFormComplete(): Boolean = length.value != null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(AR, isARMeasurement)
    }

    companion object {
        private const val AR = "ar"
    }
}
