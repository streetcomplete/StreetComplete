package de.westnordost.streetcomplete.quests.sidewalk

import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.sidewalk.LeftAndRightSidewalk
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.NO
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.SEPARATE
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.YES
import de.westnordost.streetcomplete.osm.sidewalk.floatingIcon
import de.westnordost.streetcomplete.osm.sidewalk.icon
import de.westnordost.streetcomplete.osm.sidewalk.image
import de.westnordost.streetcomplete.osm.sidewalk.title
import de.westnordost.streetcomplete.quests.AStreetSideSelectForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.street_side_select.StreetSideItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddSidewalkForm : AStreetSideSelectForm<Sidewalk, LeftAndRightSidewalk>() {

    override val otherAnswers: List<AnswerItem> = listOf(
        AnswerItem(R.string.quest_sidewalk_answer_none) { noSidewalksHereHint() }
    )

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

    private fun noSidewalksHereHint() {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_sidewalk_answer_none_title)
            .setMessage(R.string.quest_side_select_interface_explanation)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    override fun onClickSide(isRight: Boolean) {
        val items = listOf(YES, NO, SEPARATE)
        ImageListPickerDialog(requireContext(), items, R.layout.cell_icon_select_with_label_below, 2) { item ->
            streetSideSelect.replacePuzzleSide(item.value!!.asStreetSideItem()!!, isRight)
        }.show()
    }

    override fun onClickOk() {
        streetSideSelect.saveLastSelection()
        applyAnswer(LeftAndRightSidewalk(streetSideSelect.left?.value, streetSideSelect.right?.value))
    }

    override fun serialize(item: Sidewalk) = item.name
    override fun deserialize(str: String) = Sidewalk.valueOf(str)
}
