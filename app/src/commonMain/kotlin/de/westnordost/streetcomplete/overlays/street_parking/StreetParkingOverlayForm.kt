package de.westnordost.streetcomplete.overlays.street_parking

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.overlays.Edit
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.setLastPicked
import de.westnordost.streetcomplete.data.preferences.getLastPicked
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.oneway.isForwardOneway
import de.westnordost.streetcomplete.osm.oneway.isReversedOneway
import de.westnordost.streetcomplete.osm.street_parking.StreetParking
import de.westnordost.streetcomplete.osm.street_parking.applyTo
import de.westnordost.streetcomplete.osm.street_parking.parseStreetParkingSides
import de.westnordost.streetcomplete.osm.street_parking.validOrNullValues
import de.westnordost.streetcomplete.ui.common.overlay.OverlayForm
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.common.quest.LocalMapTilt
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.math.getOrientationOrZero
import org.koin.compose.koinInject

@Composable fun StreetParkingOverlayForm(
    on: (OverlayAction) -> Unit,
    element: Element,
    geometry: ElementGeometry,
    countryInfo: CountryInfo,
    preferences: Preferences = koinInject()
) {
    val favKey = "StreetParkingOverlayForm"
    val lastPicked = remember { preferences.getLastPicked<Sides<StreetParking>>(favKey) }

    val geometryRotation = remember(geometry) { geometry.getOrientationOrZero() }

    val originalParking = remember(element) {
        parseStreetParkingSides(element.tags)
            ?.validOrNullValues()
            ?: Sides<StreetParking>(null, null)
    }
    var parking by rememberSerializable(originalParking) { mutableStateOf(originalParking) }

    OverlayForm(
        on = on,
        isComplete = parking.left != null || parking.right != null,
        hasChanges = parking != originalParking,
        onClickOk = {
            preferences.setLastPicked(favKey, listOf(parking))
            val tagChanges = StringMapChangesBuilder(element.tags)
            parking.applyTo(tagChanges)
            on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
        },
        contentPadding = PaddingValues.Zero,
    ) {
        StreetParkingForm(
            value = parking,
            onValueChanged = { parking = it },
            width = element.tags["width"],
            geometryRotation = geometryRotation,
            mapRotation = LocalMapRotation.current,
            mapTilt = LocalMapTilt.current,
            isLeftHandTraffic = countryInfo.isLeftHandTraffic,
            isForwardOneway = isForwardOneway(element.tags),
            isReversedOneway = isReversedOneway(element.tags),
            lastPicked = lastPicked
        )
    }
}
