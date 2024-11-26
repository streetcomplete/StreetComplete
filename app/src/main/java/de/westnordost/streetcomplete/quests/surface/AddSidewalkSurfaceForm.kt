package de.westnordost.streetcomplete.quests.surface

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides
import de.westnordost.streetcomplete.osm.sidewalk_surface.LeftAndRightSidewalkSurface
import de.westnordost.streetcomplete.osm.surface.SELECTABLE_WAY_SURFACES
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.asStreetSideItem
import de.westnordost.streetcomplete.osm.surface.toItems
import de.westnordost.streetcomplete.quests.AStreetSideSelectForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.BOTH
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.LEFT
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.RIGHT
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog

class AddSidewalkSurfaceForm : AStreetSideSelectForm<Surface, SidewalkSurfaceAnswer>() {

    private val items: List<DisplayItem<Surface>> = SELECTABLE_WAY_SURFACES.toItems()

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_sidewalk_answer_different) { applyAnswer(SidewalkIsDifferent) }
    )

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
    override fun asStreetSideItem(item: Surface, isRight: Boolean) =
        item.asStreetSideItem(resources)
}
