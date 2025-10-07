package de.westnordost.streetcomplete.quests.lanes

import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.oneway.isForwardOneway
import de.westnordost.streetcomplete.osm.oneway.isOneway
import de.westnordost.streetcomplete.osm.oneway.isReversedOneway
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.math.getOrientationAtCenterLineInDegrees
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AddLanesForm : AbstractOsmQuestForm<LanesAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val answer: MutableState<LanesAnswer?> = mutableStateOf(null)

    private var mapRotation: MutableFloatState = mutableFloatStateOf(0f)
    private var wayRotation: MutableFloatState = mutableFloatStateOf(0f)
    private var mapTilt: MutableFloatState = mutableFloatStateOf(0f)

    override val contentPadding get() = answer.value !is MarkedLanes

    // just some shortcuts

    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic
    private val edgeLine get() = countryInfo.edgeLineStyle
    private val centerLine get() = countryInfo.centerLineStyle

    private val isOneway get() = isOneway(element.tags)

    private val isForwardOneway get() = isForwardOneway(element.tags)
    private val isReversedOneway get() = isReversedOneway(element.tags)

    override val otherAnswers: List<AnswerItem> get() {
        val answers = mutableListOf<AnswerItem>()

        if (!isOneway && countryInfo.hasCenterLeftTurnLane) {
            answers.add(AnswerItem(R.string.quest_lanes_answer_lanes_center_left_turn_lane) {
                val previous = answer.value as? MarkedLanes
                answer.value = MarkedLanes(
                    backward = previous?.backward,
                    forward = previous?.forward,
                    centerLeftTurnLane = true
                )
                checkIsFormComplete()
            })
        }
        return answers
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wayRotation.floatValue = (geometry as ElementPolylinesGeometry).getOrientationAtCenterLineInDegrees()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHint(requireContext().getString(R.string.quest_street_side_puzzle_tutorial))

        snapshotFlow { answer.value }.onEach { updateContentPadding() }.launchIn(lifecycleScope)

        binding.composeViewBase.content { Surface {
            // not possible right now due to interaction Android view <-> compose
            //answer = rememberSerializable { mutableStateOf(null) }

            LanesForm(
                value = answer.value,
                onValueChanged = {
                    answer.value = it
                    checkIsFormComplete()
                },
                rotation = wayRotation.floatValue - mapRotation.floatValue,
                tilt = mapTilt.floatValue,
                isOneway = isOneway,
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

    override fun isFormComplete(): Boolean = when (val answer = answer.value) {
        null -> false
        UnmarkedLanes -> true
        is MarkedLanes -> answer.forward != null && answer.backward != null
    }

    override fun isRejectingClose() = answer.value != null

    override fun onClickOk() { answer.value?.let { applyAnswer(it) } }
}
