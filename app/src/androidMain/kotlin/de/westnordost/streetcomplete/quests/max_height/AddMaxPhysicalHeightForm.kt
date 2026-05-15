package de.westnordost.streetcomplete.quests.max_height

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.quests.AbstractArMeasureQuestForm
import de.westnordost.streetcomplete.quests.LengthForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_maxheight_below_bridge_title
import de.westnordost.streetcomplete.resources.quest_maxheight_title
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddMaxPhysicalHeightForm : AbstractArMeasureQuestForm<MaxPhysicalHeightAnswer>() {

    private val checkArSupport: ArSupportChecker by inject()
    private var isARMeasurement: Boolean = false
    private var length: MutableState<Length?> = mutableStateOf(null)

    @Composable
    override fun Content() {
        val arIsSupported = remember { checkArSupport() }

        val isBelowBridge = remember {
            element.tags["amenity"] != "parking_entrance"
            && element.tags["barrier"] != "height_restrictor"
            && element.tags["tunnel"] == null
            && element.tags["covered"] == null
            && element.tags["man_made"] != "pipeline"
        }

        QuestForm(
            title = stringResource(
                // only the "below the bridge" situation may need some context
                if (isBelowBridge) Res.string.quest_maxheight_below_bridge_title
                else Res.string.quest_maxheight_title
            ),
            isComplete = length.value != null,
            onClickOk = {
                applyAnswer(MaxPhysicalHeightAnswer(length.value!!, isARMeasurement))
            }
        ) {
            LengthForm(
                length = length.value,
                onChange = {
                    isARMeasurement = false
                    length.value = it
                },
                selectableUnits = countryInfo.lengthUnits,
                showMeasureButton = arIsSupported,
                onClickMeasure = { takeMeasurement(it, measureVertical = true) },
                modifier = Modifier.fillMaxWidth(),
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
