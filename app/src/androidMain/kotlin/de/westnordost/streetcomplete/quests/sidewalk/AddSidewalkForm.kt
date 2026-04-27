package de.westnordost.streetcomplete.quests.sidewalk

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddSidewalkForm : AbstractOsmQuestForm<Sides<Sidewalk>>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        val lastPicked = remember { prefs.getLastPicked<Sides<Sidewalk>>(this::class.simpleName!!) }
        var sidewalks by rememberSerializable { mutableStateOf(Sides<Sidewalk>(null, null)) }

        var showNoSidewalksHint by remember { mutableStateOf(false) }

        QuestForm(
            answers = Confirm(
                isComplete = sidewalks.left != null && sidewalks.right != null,
                hasChanges = sidewalks.left != null || sidewalks.right != null,
                onClick = {
                    applyAnswer(sidewalks)
                    prefs.setLastPicked(this::class.simpleName!!, listOf(sidewalks))
                }
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_sidewalk_answer_none)) { showNoSidewalksHint = true }
            ),
            contentPadding = PaddingValues.Zero,
        ) {
            SidewalkForm(
                value = sidewalks,
                onValueChanged = { sidewalks = it },
                geometryRotation = geometryRotation.floatValue,
                mapRotation = mapRotation.floatValue,
                mapTilt = mapTilt.floatValue,
                isLeftHandTraffic = countryInfo.isLeftHandTraffic,
                lastPicked = lastPicked
            )
        }

        if (showNoSidewalksHint) {
            InfoDialog(
                onDismissRequest = { showNoSidewalksHint = false },
                title = { Text(stringResource(Res.string.quest_sidewalk_answer_none_title)) },
                text = { Text(stringResource(Res.string.quest_side_select_interface_explanation)) },
            )
        }
    }
}
