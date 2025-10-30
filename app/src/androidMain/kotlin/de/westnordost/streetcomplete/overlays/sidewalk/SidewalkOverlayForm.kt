package de.westnordost.streetcomplete.overlays.sidewalk

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.applyTo
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides
import de.westnordost.streetcomplete.osm.sidewalk.validOrNullValues
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.quests.sidewalk.SidewalkForm
import de.westnordost.streetcomplete.ui.util.content
import org.koin.android.ext.android.inject
import kotlin.getValue

class SidewalkOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val prefs: Preferences by inject()

    override val contentPadding = false

    private var originalSidewalks: Sides<Sidewalk> = Sides(null, null)
    private val sidewalks: MutableState<Sides<Sidewalk>> = mutableStateOf(Sides(null, null))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalSidewalks = parseSidewalkSides(element!!.tags)?.validOrNullValues() ?: Sides(null, null)
        if (savedInstanceState == null) {
            sidewalks.value = originalSidewalks
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lastPicked by lazy { prefs.getLastPicked<Sides<Sidewalk>>(this::class.simpleName!!) }

        binding.composeViewBase.content { Surface {
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
                lastPicked = lastPicked,
                lastPickedContentPadding = PaddingValues(start = 48.dp, end = 56.dp),
            )
        } }

        checkIsFormComplete()
    }

    override fun hasChanges(): Boolean =
        sidewalks.value != originalSidewalks

    override fun isFormComplete(): Boolean =
        sidewalks.value.left != null && sidewalks.value.right != null

    override fun onClickOk() {
        prefs.setLastPicked(this::class.simpleName!!, listOf(sidewalks.value))

        val tagChanges = StringMapChangesBuilder(element!!.tags)
        sidewalks.value.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }
}
