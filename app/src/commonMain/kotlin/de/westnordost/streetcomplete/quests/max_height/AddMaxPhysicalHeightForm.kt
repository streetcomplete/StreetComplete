package de.westnordost.streetcomplete.quests.max_height

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.length.Length
import de.westnordost.streetcomplete.osm.length.LengthForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.measure.ArMeasureViewModel
import de.westnordost.streetcomplete.screens.measure.LastArMeasurementResultEffect
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddMaxPhysicalHeightForm(
    on: (QuestAction<MaxPhysicalHeightAnswer>) -> Unit,
    element: Element,
    countryInfo: CountryInfo,
) {
    val viewModel = koinViewModel<ArMeasureViewModel>()
    val arIsSupported = remember { viewModel.isSupported() }
    val lastArMeasurementResult by viewModel.measurementResult.collectAsState()

    val isBelowBridge = remember {
        element.tags["amenity"] != "parking_entrance"
        && element.tags["barrier"] != "height_restrictor"
        && element.tags["tunnel"] == null
        && element.tags["covered"] == null
        && element.tags["man_made"] != "pipeline"
    }

    var length by rememberSerializable { mutableStateOf<Length?>(null) }
    var isArMeasurement by rememberSaveable { mutableStateOf<Boolean>(false) }

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
        title = stringResource(
            // only the "below the bridge" situation may need some context
            if (isBelowBridge) Res.string.quest_maxheight_below_bridge_title
            else Res.string.quest_maxheight_title
        ),
        isComplete = length != null,
        onClickOk = { on(Answer(MaxPhysicalHeightAnswer(length!!, isArMeasurement))) },
        on = on,
    ) {
        LengthForm(
            length = length,
            onChange = {
                isArMeasurement = false
                length = it
            },
            selectableUnits = countryInfo.lengthUnits,
            showMeasureButton = arIsSupported,
            onClickMeasure = { viewModel.measure(lengthUnit = it, measureVertical = true) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
