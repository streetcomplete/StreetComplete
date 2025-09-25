package de.westnordost.streetcomplete.overlays.sidewalk

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.sidewalk.LeftAndRightSidewalk
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.NO
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.SEPARATE
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.YES
import de.westnordost.streetcomplete.osm.sidewalk.applyTo
import de.westnordost.streetcomplete.osm.sidewalk.floatingIcon
import de.westnordost.streetcomplete.osm.sidewalk.icon
import de.westnordost.streetcomplete.osm.sidewalk.image
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides
import de.westnordost.streetcomplete.osm.sidewalk.title
import de.westnordost.streetcomplete.osm.sidewalk.validOrNullValues
import de.westnordost.streetcomplete.overlays.AStreetSideSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.street_side_select.StreetSideItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class SidewalkOverlayForm : AStreetSideSelectOverlayForm<Sidewalk>() {

    private var originalSidewalk: LeftAndRightSidewalk? = null

    @Composable override fun BoxScope.DialogItemContent(item: Sidewalk, isRight: Boolean) {
        val icon = item.icon
        val title = item.title
        if (icon != null && title != null) {
            ImageWithLabel(painterResource(icon), stringResource(title))
        }
    }

    @Composable override fun getStreetSideItem(item: Sidewalk, isRight: Boolean) = StreetSideItem(
        image = item.image?.let { painterResource(it) },
        title = item.title?.let { stringResource(it) },
        floatingIcon = item.floatingIcon?.let { painterResource(it) }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        originalSidewalk = parseSidewalkSides(element!!.tags)?.validOrNullValues()
        if (savedInstanceState == null) {
            initStateFromTags()
        }
    }

    private fun initStateFromTags() {
        streetSideSelect.setPuzzleSide(originalSidewalk?.left?.asStreetSideItem(), false)
        streetSideSelect.setPuzzleSide(originalSidewalk?.right?.asStreetSideItem(), true)
    }

    override fun onClickSide(isRight: Boolean) {
        val items = listOf(YES, NO, SEPARATE).mapNotNull { it.asItem() }
        ImageListPickerDialog(requireContext(), items, R.layout.cell_icon_select_with_label_below, 2) { item ->
            streetSideSelect.replacePuzzleSide(item.value!!.asStreetSideItem()!!, isRight)
        }.show()
    }

    override fun onClickOk() {
        streetSideSelect.saveLastSelection()
        val sidewalks = LeftAndRightSidewalk(streetSideSelect.left?.value, streetSideSelect.right?.value)
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        sidewalks.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }

    override fun hasChanges(): Boolean =
        streetSideSelect.left?.value != originalSidewalk?.left ||
        streetSideSelect.right?.value != originalSidewalk?.right

    override fun serialize(item: Sidewalk) = item.name
    override fun deserialize(str: String) = Sidewalk.valueOf(str)
}
