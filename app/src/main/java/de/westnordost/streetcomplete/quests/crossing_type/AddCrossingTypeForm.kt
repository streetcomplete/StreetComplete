package de.westnordost.streetcomplete.quests.crossing_type

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.crossing_type.CrossingType.*
import de.westnordost.streetcomplete.view.image_select.Item

class AddCrossingTypeForm : AImageListQuestAnswerFragment<CrossingType, CrossingType>() {

    override val items = listOf(
        Item(TRAFFIC_SIGNALS, R.drawable.crossing_type_signals, R.string.quest_crossing_type_signals_controlled),
        Item(MARKED, R.drawable.crossing_type_zebra, R.string.quest_crossing_type_marked),
        Item(UNMARKED, R.drawable.crossing_type_unmarked, R.string.quest_crossing_type_unmarked)
    )

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_crossing_type_no_crossing) {
            activity?.let { AlertDialog.Builder(it)
                .setMessage(R.string.quest_crossing_type_explain_delete_crossing_note)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    composeNote("This crossing does not exist.")
                }
                .show()
            }
                                                              },
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<CrossingType>) {
        applyAnswer(selectedItems.single())
    }
}
