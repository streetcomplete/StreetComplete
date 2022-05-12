package de.westnordost.streetcomplete.quests.sidewalk

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.NO
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.SEPARATE
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.YES
import de.westnordost.streetcomplete.osm.sidewalk.SidewalkSides
import de.westnordost.streetcomplete.quests.AStreetSideSelectForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddSidewalkForm : AStreetSideSelectForm<Sidewalk, SidewalkSides>() {

    override val items = listOf(YES, NO, SEPARATE)

    override fun getDisplayItem(value: Sidewalk) = value.asStreetSideItem()

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

    override fun onClickOk(leftSide: Sidewalk?, rightSide: Sidewalk?) {
        applyAnswer(SidewalkSides(leftSide!!, rightSide!!))
    }
}
