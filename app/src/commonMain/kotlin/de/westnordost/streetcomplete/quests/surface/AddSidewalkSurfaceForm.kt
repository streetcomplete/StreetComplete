package de.westnordost.streetcomplete.quests.surface

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.any
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides
import de.westnordost.streetcomplete.osm.sidewalk_surface.SidewalkSurface
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.common.quest.LocalMapTilt
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.math.getOrientationOrZero
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.collections.emptyList

@Composable
fun AddSidewalkSurfaceForm(
    onAnswer: (SidewalkSurfaceAnswer) -> Unit,
    element: Element,
    geometry: ElementGeometry,
    countryInfo: CountryInfo,
    preferences: Preferences = koinInject(),
) {
    val favKey = "AddSidewalkSurfaceForm"
    val sidewalk = remember { parseSidewalkSides(element.tags) }
    val hasSidewalkLeft = sidewalk?.left == Sidewalk.YES
    val hasSidewalkRight = sidewalk?.right == Sidewalk.YES
    val geometryRotation = remember(geometry) { geometry.getOrientationOrZero() }

    val lastPicked = remember {
        if (hasSidewalkLeft && hasSidewalkRight) {
            preferences.getLastPicked<Sides<Surface>>(favKey)
                .takeFavorites(n = 5, history = 15, first = 1)
        } else {
            emptyList()
        }
    }

    var sidewalkSurfaces by rememberSerializable { mutableStateOf(Sides<Surface>(null, null)) }

    QuestForm(
        isComplete =
            (!hasSidewalkLeft || sidewalkSurfaces.left != null) &&
            (!hasSidewalkRight || sidewalkSurfaces.right != null),
        hasChanges =
            sidewalkSurfaces.any { it != null },
        onClickOk = {
            if (hasSidewalkLeft && hasSidewalkRight) {
                preferences.setLastPicked(favKey, listOf(sidewalkSurfaces))
            }
            onAnswer(SidewalkSurfaceAnswer.Surfaces(SidewalkSurface(sidewalkSurfaces)))
        },
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_sidewalk_answer_different)) {
                onAnswer(SidewalkSurfaceAnswer.SidewalkIsDifferent)
            }
        ),
        contentPadding = PaddingValues.Zero
    ) {
        SidewalkSurfaceForm(
            value = sidewalkSurfaces,
            onValueChanged = { sidewalkSurfaces = it },
            geometryRotation = geometryRotation,
            mapRotation = LocalMapRotation.current,
            mapTilt = LocalMapTilt.current,
            isLeftHandTraffic = countryInfo.isLeftHandTraffic,
            lastPicked = lastPicked,
            hasSidewalkLeft = hasSidewalkLeft,
            hasSidewalkRight = hasSidewalkRight,
        )
    }
}
