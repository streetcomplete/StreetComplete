package de.westnordost.streetcomplete.overlays.street_parking

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.oneway.isForwardOneway
import de.westnordost.streetcomplete.osm.oneway.isReversedOneway
import de.westnordost.streetcomplete.osm.street_parking.StreetParking
import de.westnordost.streetcomplete.osm.street_parking.applyTo
import de.westnordost.streetcomplete.osm.street_parking.parseStreetParkingSides
import de.westnordost.streetcomplete.osm.street_parking.validOrNullValues
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.ui.common.overlay.OverlayForm
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.common.quest.LocalMapTilt
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.math.getOrientationAtCenterLineInDegrees
import de.westnordost.streetcomplete.util.math.getOrientationOrZero
import org.koin.android.ext.android.inject

class StreetParkingOverlayForm : AbstractOverlayForm() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        val lastPicked = remember {
            prefs.getLastPicked<Sides<StreetParking>>("StreetParkingOverlayForm")
        }

        val geometryRotation = remember(geometry) { geometry.getOrientationOrZero() }

        val tags = element!!.tags
        val originalParking = remember {
            parseStreetParkingSides(tags)
                ?.validOrNullValues()
                ?: Sides<StreetParking>(null, null)
        }
        var parking by rememberSerializable { mutableStateOf(originalParking) }

        OverlayForm(
            isComplete = parking.left != null || parking.right != null,
            hasChanges = parking != originalParking,
            onClickOk = {
                prefs.setLastPicked("StreetParkingOverlayForm", listOf(parking))
                val tagChanges = StringMapChangesBuilder(tags)
                parking.applyTo(tagChanges)
                applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
            },
            contentPadding = PaddingValues.Zero
        ) {

            StreetParkingForm(
                value = parking,
                onValueChanged = { parking = it },
                width = tags["width"],
                geometryRotation = geometryRotation,
                mapRotation = LocalMapRotation.current,
                mapTilt = LocalMapTilt.current,
                isLeftHandTraffic = countryInfo.isLeftHandTraffic,
                isForwardOneway = isForwardOneway(tags),
                isReversedOneway = isReversedOneway(tags),
                lastPicked = lastPicked
            )
        }
    }
}
