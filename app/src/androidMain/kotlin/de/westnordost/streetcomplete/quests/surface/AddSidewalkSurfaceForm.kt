package de.westnordost.streetcomplete.quests.surface

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides
import de.westnordost.streetcomplete.osm.sidewalk_surface.LeftAndRightSidewalkSurface
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.quests.AStreetSideSelectForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.sidewalk_illustration_yes
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.street_side_select.StreetSideItem
import de.westnordost.streetcomplete.ui.util.ClipCirclePainter
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.BOTH
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.LEFT
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.RIGHT
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddSidewalkSurfaceForm : AStreetSideSelectForm<Surface, SidewalkSurfaceAnswer>() {

    private val items: List<Surface> = Surface.selectableValuesForWays

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_sidewalk_answer_different) { applyAnswer(SidewalkIsDifferent) }
    )

    @Composable override fun BoxScope.DialogItemContent(item: Surface, isRight: Boolean) {
        val icon = item.icon
        if (icon != null) {
            ImageWithLabel(painterResource(icon), stringResource(item.title))
        }
    }

    @Composable override fun getStreetSideItem(item: Surface, isRight: Boolean): StreetSideItem {
        val icon = item.icon?.let { painterResource(it) }
        return StreetSideItem(
            image = painterResource(Res.drawable.sidewalk_illustration_yes),
            title = stringResource(item.title),
            floatingIcon = remember(icon) { icon?.let { ClipCirclePainter(it) } }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            initStateFromTags()
        }
    }

    private fun initStateFromTags() {
        val sides = parseSidewalkSides(element.tags)
        val hasLeft = sides?.left == Sidewalk.YES
        val hasRight = sides?.right == Sidewalk.YES

        streetSideSelect.showSides = when {
            hasLeft && hasRight -> BOTH
            hasLeft -> LEFT
            hasRight -> RIGHT
            else -> return
        }
    }

    override fun onClickSide(isRight: Boolean) {
        ImageListPickerDialog(requireContext(), items, R.layout.cell_labeled_icon_select, 2) {
            val surface = it.value!!
            replaceSurfaceSide(isRight, surface)
        }.show()
    }

    private fun replaceSurfaceSide(isRight: Boolean, surface: Surface) {
        val streetSideItem = surface.asStreetSideItem(requireContext().resources)
        streetSideSelect.replacePuzzleSide(streetSideItem, isRight)
    }

    override fun onClickOk() {
        val left = streetSideSelect.left?.value
        val right = streetSideSelect.right?.value
        streetSideSelect.saveLastSelection()
        applyAnswer(SidewalkSurface(LeftAndRightSidewalkSurface(left, right)))
    }

    /* ------------------------------------------------------------------------------------------ */

    override fun serialize(item: Surface) = item.name
    override fun deserialize(str: String) = Surface.valueOf(str)
}
