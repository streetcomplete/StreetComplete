package de.westnordost.streetcomplete.overlays.street_parking

import android.os.Bundle
import android.view.View
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.oneway.isForwardOneway
import de.westnordost.streetcomplete.osm.oneway.isReversedOneway
import de.westnordost.streetcomplete.osm.street_parking.StreetParking
import de.westnordost.streetcomplete.osm.street_parking.applyTo
import de.westnordost.streetcomplete.osm.street_parking.parseStreetParkingSides
import de.westnordost.streetcomplete.osm.street_parking.validOrNullValues
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.ui.util.content
import org.koin.android.ext.android.inject

class StreetParkingOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val prefs: Preferences by inject()

    override val contentPadding = false

    private var originalParking: Sides<StreetParking> = Sides(null, null)
    private val parking: MutableState<Sides<StreetParking>> = mutableStateOf(Sides(null, null))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalParking = parseStreetParkingSides(element!!.tags)
            ?.validOrNullValues()
            ?: Sides(null, null)
        if (savedInstanceState == null) {
            parking.value = originalParking
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lastPicked by lazy { prefs.getLastPicked<Sides<StreetParking>>(this::class.simpleName!!) }

        binding.composeViewBase.content { Surface {
            StreetParkingForm(
                value = parking.value,
                onValueChanged = {
                    parking.value = it
                    checkIsFormComplete()
                },
                width = element?.tags?.get("width"),
                geometryRotation = geometryRotation.floatValue,
                mapRotation = mapRotation.floatValue,
                mapTilt = mapTilt.floatValue,
                isLeftHandTraffic = countryInfo.isLeftHandTraffic,
                isForwardOneway = isForwardOneway(element!!.tags),
                isReversedOneway = isReversedOneway(element!!.tags),
                lastPicked = lastPicked
            )
        } }
        checkIsFormComplete()
    }

    /* --------------------------------------- apply answer ------------------------------------- */

    override fun hasChanges(): Boolean =
        parking.value != originalParking

    override fun isFormComplete(): Boolean =
        parking.value.left != null || parking.value.right != null

    override fun onClickOk() {
        prefs.setLastPicked(this::class.simpleName!!, listOf(parking.value))
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        parking.value.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }
}
