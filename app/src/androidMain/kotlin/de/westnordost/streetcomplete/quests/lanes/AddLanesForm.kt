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

    override val contentPadding = false

    // just some shortcuts

    private val edgeLineStyle by lazy {
        when {
            countryInfo.edgeLineStyle.contains("short dashes") -> LineStyle.SHORT_DASHES
            countryInfo.edgeLineStyle.contains("dashes") -> LineStyle.DASHES
            else -> LineStyle.CONTINUOUS
        }
    }

    private val edgeLineColor by lazy {
        if (countryInfo.edgeLineStyle.contains("yellow")) Color.Yellow else Color.White
    }

    private val centerLineColor by lazy {
        if (countryInfo.centerLineStyle.contains("yellow")) Color.Yellow else Color.White
    }

    private val isOneway by lazy { isOneway(element.tags) }

    private val isReversedOneway by lazy { isReversedOneway(element.tags) }

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
                wayRotation = geometryRotation.floatValue,
                mapRotation = mapRotation.floatValue,
                mapTilt = mapTilt.floatValue,
                isOneway = isOneway,
                isReversedOneway = isReversedOneway,
                isLeftHandTraffic = countryInfo.isLeftHandTraffic,
                centerLineColor = centerLineColor,
                edgeLineColor = edgeLineColor,
                edgeLineStyle = edgeLineStyle,
            )
        } }
    }

    override fun isFormComplete(): Boolean =
        if (!isOneway) {
            answer.value.forward != null && answer.value.backward != null
        } else {
            answer.value.forward != null || answer.value.backward != null
        }

    override fun isRejectingClose(): Boolean =
        answer.value.forward != null || answer.value.backward != null

    override fun onClickOk() {
        applyAnswer(answer.value)
    }
}
