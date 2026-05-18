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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.hasDubiousRoadWidth
import de.westnordost.streetcomplete.quests.AbstractArMeasureQuestForm
import de.westnordost.streetcomplete.quests.LengthForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject
import org.koin.compose.koinInject

@Composable
fun AddWidthForm(
    onAnswer: (WidthAnswer) -> Unit,
    element: Element,
    countryInfo: CountryInfo,
    checkArSupport: ArSupportChecker = koinInject()
) {
    var confirmDubiousRoadWidth by remember { mutableStateOf(false) }

    val isRoad = remember(element) { element.tags["highway"] in ALL_ROADS }
    val arIsSupported = remember { checkArSupport() }

    var length by rememberSerializable { mutableStateOf<Length?>(null) }
    var isArMeasurement by rememberSaveable { mutableStateOf<Boolean>(false) }

    // TODO compose-quest-form not called from anywhere yet
    fun onTookMeasurement(len: Length) {
        length = len
        isArMeasurement = true
    }

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
        },
        content = {
            Column {
                if(isRoad) {
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
                    onClickMeasure = { takeMeasurement(it, measureVertical = false) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    )

    if (confirmDubiousRoadWidth) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmDubiousRoadWidth = false },
            onConfirmed = { onAnswer(WidthAnswer(length!!, isArMeasurement)) },
            text = { Text(stringResource(Res.string.quest_road_width_unusualInput_confirmation_description)) }
        )
    }
}
