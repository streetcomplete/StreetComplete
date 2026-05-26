package de.westnordost.streetcomplete.quests.width

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.hasDubiousRoadWidth
import de.westnordost.streetcomplete.quests.LengthForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.measure.ArMeasureViewModel
import de.westnordost.streetcomplete.screens.measure.LastArMeasurementResultEffect
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddWidthForm(
    onAnswer: (WidthAnswer) -> Unit,
    element: Element,
    countryInfo: CountryInfo,
) {
    val viewModel = koinViewModel<ArMeasureViewModel>()
    val arIsSupported = remember { viewModel.isSupported() }
    val lastArMeasurementResult by viewModel.measurementResult.collectAsState()

    val isRoad = remember(element) { element.tags["highway"] in ALL_ROADS }

    var length by rememberSerializable { mutableStateOf<Length?>(null) }
    var isArMeasurement by rememberSaveable { mutableStateOf<Boolean>(false) }

    var confirmDubiousRoadWidth by remember { mutableStateOf(false) }

    LastArMeasurementResultEffect(
        lastResult = lastArMeasurementResult,
        onMeasureSuccess = {
            length = it
            isArMeasurement = true
            viewModel.resetMeasurementResult()
        },
        onConfirmDisableArQuests = {
            viewModel.disableArQuests()
            viewModel.resetMeasurementResult()
        }
    )

    QuestForm(
        isComplete = length != null,
        onClickOk = {
            val length = length!!
            val newTags = element.tags + ("width" to length.toMeters().toString())
            if (hasDubiousRoadWidth(newTags) != true) {
                onAnswer(WidthAnswer(length, isArMeasurement))
            } else {
                confirmDubiousRoadWidth = true
            }
        }
    ) {
        Column {
            if (isRoad) {
                Text(stringResource(Res.string.quest_road_width_explanation))
            }

            LengthForm(
                length = length,
                onChange = {
                    isArMeasurement = false
                    length = it
                },
                selectableUnits = countryInfo.lengthUnits,
                showMeasureButton = arIsSupported,
                onClickMeasure = { viewModel.measure(lengthUnit = it, measureVertical = false) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (confirmDubiousRoadWidth) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmDubiousRoadWidth = false },
            onConfirmed = { onAnswer(WidthAnswer(length!!, isArMeasurement)) },
            text = { Text(stringResource(Res.string.quest_road_width_unusualInput_confirmation_description)) }
        )
    }
}
