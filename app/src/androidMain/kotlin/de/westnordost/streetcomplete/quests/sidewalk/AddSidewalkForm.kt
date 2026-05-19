package de.westnordost.streetcomplete.quests.sidewalk

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.LastPickedChipsRowViewModel
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.common.quest.LocalMapTilt
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.math.getOrientationOrZero
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddSidewalkForm(
    onAnswer: (Sides<Sidewalk>) -> Unit,
    geometry: ElementGeometry,
    countryInfo: CountryInfo,
) {
    val favKey = "AddSidewalkForm"
    val lastPickedViewModel = koinViewModel<LastPickedChipsRowViewModel>()
    val lastPicked = remember { lastPickedViewModel.getFavorites<Sides<Sidewalk>>(favKey) }

    val geometryRotation = remember(geometry) { geometry.getOrientationOrZero() }

    var sidewalks by rememberSerializable { mutableStateOf(Sides<Sidewalk>(null, null)) }

    var showNoSidewalksHint by remember { mutableStateOf(false) }

    QuestForm(
        isComplete = sidewalks.left != null && sidewalks.right != null,
        hasChanges = sidewalks.left != null || sidewalks.right != null,
        onClickOk = {
            onAnswer(sidewalks)
            lastPickedViewModel.setFavorites(favKey, listOf(sidewalks))
        },
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_sidewalk_answer_none)) { showNoSidewalksHint = true }
        ),
        contentPadding = PaddingValues.Zero,
    ) {
        SidewalkForm(
            value = sidewalks,
            onValueChanged = { sidewalks = it },
            geometryRotation = geometryRotation,
            mapRotation = LocalMapRotation.current,
            mapTilt = LocalMapTilt.current,
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
