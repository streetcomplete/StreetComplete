package de.westnordost.streetcomplete.overlays.sidewalk

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.applyTo
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides
import de.westnordost.streetcomplete.osm.sidewalk.validOrNullValues
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.quests.sidewalk.SidewalkForm
import de.westnordost.streetcomplete.ui.common.overlay.OverlayForm
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.common.quest.LocalMapTilt
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.math.getOrientationOrZero
import org.koin.android.ext.android.inject

class SidewalkOverlayForm : AbstractOverlayForm() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        val lastPicked = remember {
            prefs.getLastPicked<Sides<Sidewalk>>("SidewalkOverlayForm")
        }
        val originalSidewalks = remember {
            parseSidewalkSides(element!!.tags)
                ?.validOrNullValues()
                ?: Sides<Sidewalk>(null, null)
        }
        val geometryRotation = remember(geometry) { geometry.getOrientationOrZero() }

        var sidewalks by rememberSerializable { mutableStateOf(originalSidewalks) }

        OverlayForm(
            isComplete = sidewalks.left != null || sidewalks.right != null,
            hasChanges = sidewalks != originalSidewalks,
            onClickOk = {
                prefs.setLastPicked("SidewalkOverlayForm", listOf(sidewalks))
                val tagChanges = StringMapChangesBuilder(element!!.tags)
                sidewalks.applyTo(tagChanges)
                applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
            },
            contentPadding = PaddingValues.Zero,
        ) {
            SidewalkForm(
                value = sidewalks,
                onValueChanged = { sidewalks = it },
                geometryRotation = geometryRotation,
                mapRotation = LocalMapRotation.current,
                mapTilt = LocalMapTilt.current,
                isLeftHandTraffic = countryInfo.isLeftHandTraffic,
                lastPicked = lastPicked,
                lastPickedContentPadding = PaddingValues(start = 48.dp, end = 56.dp),
            )
        }
    }
}
