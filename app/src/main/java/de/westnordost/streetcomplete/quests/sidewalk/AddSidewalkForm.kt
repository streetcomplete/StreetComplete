package de.westnordost.streetcomplete.quests.sidewalk

import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.SidewalkSides
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AStreetSideSelectFragment
import de.westnordost.streetcomplete.quests.AnswerItem

class AddSidewalkForm : AStreetSideSelectFragment<Sidewalk, SidewalkSides>() {

    override val items = Sidewalk.values().map { it.asStreetSideItem() }

    override val otherAnswers: List<AnswerItem> = listOf(
        AnswerItem(R.string.quest_sidewalk_answer_none) { noSidewalksHereHint() }
    )

    private fun noSidewalksHereHint() {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_sidewalk_answer_none_title)
            .setMessage(R.string.quest_side_select_interface_explanation)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    override fun onClickOk(leftSide: Sidewalk, rightSide: Sidewalk) {
        applyAnswer(SidewalkSides(leftSide, rightSide))
    }
}
