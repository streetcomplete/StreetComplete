package de.westnordost.streetcomplete.quests.width

import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.hasDubiousRoadWidth
import de.westnordost.streetcomplete.quests.AbstractArMeasureQuestForm
import de.westnordost.streetcomplete.quests.LengthForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddWidthForm : AbstractArMeasureQuestForm<WidthAnswer>() {

    private val checkArSupport: ArSupportChecker by inject()
    private var isARMeasurement: Boolean = false
    private var length: MutableState<Length?> = mutableStateOf(null)

    @Composable
    override fun Content() {
        var confirmDubiousRoadWidth by remember { mutableStateOf(false) }

        val isRoad = remember { element.tags["highway"] in ALL_ROADS }
        val arIsSupported = remember { checkArSupport() }

        QuestForm(
            answers = Confirm(
                isComplete = length.value != null,
                onClick = {
                    val length = length.value!!
                    val newTags = element.tags + ("width" to length.toMeters().toString())
                    if (hasDubiousRoadWidth(newTags) != true) {
                        applyAnswer(WidthAnswer(length, isARMeasurement))
                    } else {
                        confirmDubiousRoadWidth = true
                    }
                }
            ),
        ) {
            Column {
                if(isRoad) {
                    Text(stringResource(Res.string.quest_road_width_explanation))
                }

                LengthForm(
                    length = length.value,
                    onChange = {
                        isARMeasurement = false
                        length.value = it
                    },
                    selectableUnits = countryInfo.lengthUnits,
                    showMeasureButton = arIsSupported,
                    onClickMeasure = { takeMeasurement(it, measureVertical = false) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (confirmDubiousRoadWidth) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmDubiousRoadWidth = false },
                onConfirmed = { applyAnswer(WidthAnswer(length.value!!, isARMeasurement)) },
                text = { Text(stringResource(Res.string.quest_road_width_unusualInput_confirmation_description)) }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let { isARMeasurement = it.getBoolean(AR) }
    }

    override fun onMeasured(length: Length) {
        isARMeasurement = true
        this.length.value = length
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(AR, isARMeasurement)
    }

    companion object {
        private const val AR = "ar"
    }
}
