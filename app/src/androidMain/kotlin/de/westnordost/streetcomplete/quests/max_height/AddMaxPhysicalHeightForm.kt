package de.westnordost.streetcomplete.quests.max_height

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxWidth
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
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.quests.AbstractArMeasureQuestForm
import de.westnordost.streetcomplete.quests.LengthForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_maxheight_below_bridge_title
import de.westnordost.streetcomplete.resources.quest_maxheight_title
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject
import org.koin.compose.koinInject
import kotlin.text.get

@Composable
fun AddMaxPhysicalHeightForm(
    onAnswer: (MaxPhysicalHeightAnswer) -> Unit,
    element: Element,
    countryInfo: CountryInfo,
    checkArSupport: ArSupportChecker = koinInject()
) {
    val arIsSupported = remember { checkArSupport() }

    val isBelowBridge = remember {
        element.tags["amenity"] != "parking_entrance"
        && element.tags["barrier"] != "height_restrictor"
        && element.tags["tunnel"] == null
        && element.tags["covered"] == null
        && element.tags["man_made"] != "pipeline"
    }

    var length by rememberSerializable { mutableStateOf<Length?>(null) }
    var isArMeasurement by rememberSaveable { mutableStateOf<Boolean>(false) }

    // TODO compose-quest-form not called from anywhere yet
    fun onTookMeasurement(len: Length) {
        length = len
        isArMeasurement = true
    }

    QuestForm(
        title = stringResource(
            // only the "below the bridge" situation may need some context
            if (isBelowBridge) Res.string.quest_maxheight_below_bridge_title
            else Res.string.quest_maxheight_title
        ),
        isComplete = length != null,
        onClickOk = { onAnswer(MaxPhysicalHeightAnswer(length!!, isArMeasurement)) }
    ) {
        LengthForm(
            length = length,
            onChange = {
                isArMeasurement = false
                length = it
            },
            selectableUnits = countryInfo.lengthUnits,
            showMeasureButton = arIsSupported,
            onClickMeasure = { takeMeasurement(it, measureVertical = true) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
