package de.westnordost.streetcomplete.quests.sidewalk

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.koin.android.ext.android.inject
import kotlin.getValue

class AddSidewalkForm : AbstractOsmQuestForm<Sides<Sidewalk>>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val prefs: Preferences by inject()

    override val contentPadding = false

    override val otherAnswers: List<AnswerItem> = listOf(
        AnswerItem(R.string.quest_sidewalk_answer_none) { noSidewalksHereHint() }
    )

    private lateinit var sidewalks: MutableState<Sides<Sidewalk>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lastPicked by lazy { prefs.getLastPicked<Sides<Sidewalk>>(this::class.simpleName!!) }

        binding.composeViewBase.content { Surface {
            sidewalks = rememberSerializable { mutableStateOf(Sides(null, null)) }

            SidewalkForm(
                value = sidewalks.value,
                onValueChanged = {
                    sidewalks.value = it
                    checkIsFormComplete()
                },
                geometryRotation = geometryRotation.floatValue,
                mapRotation = mapRotation.floatValue,
                mapTilt = mapTilt.floatValue,
                isLeftHandTraffic = countryInfo.isLeftHandTraffic,
                lastPicked = lastPicked
            )

            checkIsFormComplete()
        } }
    }

    private fun noSidewalksHereHint() {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_sidewalk_answer_none_title)
            .setMessage(R.string.quest_side_select_interface_explanation)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    override fun isFormComplete() =
        sidewalks.value.left != null && sidewalks.value.right != null

    override fun isRejectingClose() =
        sidewalks.value.left != null || sidewalks.value.right != null

    override fun onClickOk() {
        applyAnswer(sidewalks.value)
        prefs.setLastPicked(this::class.simpleName!!, listOf(sidewalks.value))
    }
}
