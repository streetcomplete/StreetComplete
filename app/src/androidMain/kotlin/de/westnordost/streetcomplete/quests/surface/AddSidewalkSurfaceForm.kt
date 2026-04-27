package de.westnordost.streetcomplete.quests.surface

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.any
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides
import de.westnordost.streetcomplete.osm.sidewalk_surface.SidewalkSurface
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddSidewalkSurfaceForm : AbstractOsmQuestForm<SidewalkSurfaceAnswer>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        val sidewalk = remember { parseSidewalkSides(element.tags) }
        val hasSidewalkLeft = sidewalk?.left == Sidewalk.YES
        val hasSidewalkRight = sidewalk?.right == Sidewalk.YES

        val lastPicked = remember {
            if (hasSidewalkLeft && hasSidewalkRight) {
                prefs.getLastPicked<Sides<Surface>>(this::class.simpleName!!)
            } else {
                emptyList()
            }
        }

        var sidewalkSurfaces by rememberSerializable { mutableStateOf(Sides<Surface>(null, null)) }

        QuestForm(
            answers = Confirm(
                isComplete =
                    (!hasSidewalkLeft || sidewalkSurfaces.left != null) &&
                    (!hasSidewalkRight || sidewalkSurfaces.right != null),
                hasChanges =
                    sidewalkSurfaces.any { it != null }
            ) {
                applyAnswer(SidewalkSurfaceAnswer.Surfaces(SidewalkSurface(sidewalkSurfaces)))
                if (hasSidewalkLeft && hasSidewalkRight) {
                    prefs.setLastPicked(this::class.simpleName!!, listOf(sidewalkSurfaces))
                }
            },
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_sidewalk_answer_different)) {
                    applyAnswer(SidewalkSurfaceAnswer.SidewalkIsDifferent)
                }
            ),
            contentPadding = PaddingValues.Zero
        ) {
            SidewalkSurfaceForm(
                value = sidewalkSurfaces,
                onValueChanged = { sidewalkSurfaces = it },
                geometryRotation = geometryRotation.floatValue,
                mapRotation = mapRotation.floatValue,
                mapTilt = mapTilt.floatValue,
                isLeftHandTraffic = countryInfo.isLeftHandTraffic,
                lastPicked = lastPicked,
                hasSidewalkLeft = hasSidewalkLeft,
                hasSidewalkRight = hasSidewalkRight,
            )
        }
    }
}
