package de.westnordost.streetcomplete.quests.max_height

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.length.Length
import de.westnordost.streetcomplete.osm.length.LengthForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.measure.ArMeasureResult
import de.westnordost.streetcomplete.ui.util.measure.ArMeasureViewModel
import de.westnordost.streetcomplete.ui.util.measure.LastArMeasurementResultEffect
import de.westnordost.streetcomplete.ui.util.measure.rememberArMeasureAppLauncher
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
    val arMeasureAppLauncher = rememberArMeasureAppLauncher()

    // We can't actually check if the element with the given tags is below another way here.
    // What we do instead, is, by knowledge of the quest filter used (see above),
    // we infer that if the element has the following tags, the quest must have been created
    // because it crosses with another way above it and not because it itself is a tunnel etc.
    val isBelowWay = remember {
        element.tags["amenity"] != "parking_entrance"
        && element.tags["barrier"] != "height_restrictor"
        && element.tags["tunnel"] == null
        && element.tags["covered"] == null
    }

    var length by rememberSerializable { mutableStateOf<Length?>(null) }
    var lastArMeasurementResult by remember { mutableStateOf<ArMeasureResult?>(null) }
    var isArMeasurement by rememberSaveable { mutableStateOf<Boolean>(false) }

    LastArMeasurementResultEffect(
        lastResult = lastArMeasurementResult,
        onConfirmDisableArQuests = {
            viewModel.disableArQuests()
            lastArMeasurementResult = null
        }
    )

    fun onMeasureResult(result: ArMeasureResult) {
        if (result is ArMeasureResult.Success) {
            length = result.length
            isArMeasurement = true
        }
    }

    QuestForm(
        on = on,
        isComplete = length != null,
        onClickOk = { on(Answer(MaxPhysicalHeightAnswer(length!!, isArMeasurement))) },
        title = stringResource(
            // only the "below the bridge" situation may need some context
            if (isBelowWay) Res.string.quest_maxheight_below_bridge_title
            else Res.string.quest_maxheight_title
        ),
        hintText =
            if (element.type == ElementType.WAY && !tunnelFilter.matches(element)) {
                stringResource(Res.string.quest_maxheight_split_way_hint,
                    stringResource(Res.string.quest_generic_answer_differs_along_the_way)
                )
            } else {
                null
            },
    ) {
        LengthForm(
            length = length,
            onChange = {
                isArMeasurement = false
                length = it
            },
            selectableUnits = countryInfo.lengthUnits,
            showMeasureButton = arIsSupported,
            onClickMeasure = { lengthUnit ->
                arMeasureAppLauncher.measure(
                    lengthUnit = lengthUnit,
                    measureVertical = false,
                    onResult = ::onMeasureResult
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
