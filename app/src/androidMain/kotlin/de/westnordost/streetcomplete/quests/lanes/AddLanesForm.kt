package de.westnordost.streetcomplete.quests.lanes

import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.oneway.isOneway
import de.westnordost.streetcomplete.osm.oneway.isReversedOneway
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.math.getOrientationAtCenterLineInDegrees

class AddLanesForm : AbstractOsmQuestForm<LanesAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private lateinit var answer: MutableState<Lanes>

    private var mapRotation: MutableFloatState = mutableFloatStateOf(0f)
    private var wayRotation: MutableFloatState = mutableFloatStateOf(0f)
    private var mapTilt: MutableFloatState = mutableFloatStateOf(0f)

    override val contentPadding = false

    // just some shortcuts

    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic
    private val edgeLine get() = countryInfo.edgeLineStyle
    private val centerLine get() = countryInfo.centerLineStyle

    private val isOneway get() = isOneway(element.tags)

    private val isReversedOneway get() = isReversedOneway(element.tags)

    override val otherAnswers: List<AnswerItem> get() = buildList {
        if (!isOneway && countryInfo.hasCenterLeftTurnLane) {
            add(AnswerItem(R.string.quest_lanes_answer_lanes_center_left_turn_lane) {
                answer.value = answer.value.copy(centerLeftTurnLane = true)
                checkIsFormComplete()
            })
        }
        add(AnswerItem(R.string.quest_lanes_answer_noLanes) {
            applyAnswer(LanesAnswer.IsUnmarked)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wayRotation.floatValue = (geometry as ElementPolylinesGeometry).getOrientationAtCenterLineInDegrees()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            answer = rememberSerializable { mutableStateOf(Lanes()) }

            LanesForm(
                value = answer.value,
                onValueChanged = {
                    answer.value = it
                    checkIsFormComplete()
                },
                rotation = wayRotation.floatValue - mapRotation.floatValue,
                tilt = mapTilt.floatValue,
                isOneway = isOneway,
                isReversedOneway = isReversedOneway,
                isLeftHandTraffic = isLeftHandTraffic,
                centerLineColor = if (centerLine.contains("yellow")) Color.Yellow else Color.White,
                edgeLineColor = if (edgeLine.contains("yellow")) Color.Yellow else Color.White,
                edgeLineStyle = when {
                    edgeLine.contains("short dashes") -> LineStyle.SHORT_DASHES
                    edgeLine.contains("dashes") -> LineStyle.DASHES
                    else -> LineStyle.CONTINUOUS
                },
            )
        } }
    }

    @AnyThread override fun onMapOrientation(rotation: Double, tilt: Double) {
        mapRotation.floatValue = rotation.toFloat()
        mapTilt.floatValue = tilt.toFloat()
    }

    override fun isFormComplete(): Boolean =
        if (!isOneway) {
            answer.value.forward != null && answer.value.backward != null
        } else {
            answer.value.forward != null || answer.value.backward != null
        }

    override fun isRejectingClose(): Boolean =
        answer.value.forward != null || answer.value.backward != null

    override fun onClickOk() { answer.value?.let { applyAnswer(it) } }
}
