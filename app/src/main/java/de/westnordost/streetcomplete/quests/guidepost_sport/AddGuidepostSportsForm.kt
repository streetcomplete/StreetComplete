package de.westnordost.streetcomplete.quests.guidepost_sport

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.ImageSelectAdapter
import de.westnordost.streetcomplete.view.image_select.Item

class AddGuidepostSportsForm : AImageListQuestForm<List<GuidepostSport>, GuidepostSportsAnswer>() {

    override val descriptionResId = R.string.quest_guidepost_sports_note

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_guidepost_sports_answer_simple) { confirmJustSimple() }
    )

    override val items get() = GuidepostSport.selectableValues.map { it.asItem() }
    override val itemsPerRow = 3
    override val maxSelectableItems = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
        imageSelector.listeners.add(object : ImageSelectAdapter.OnItemSelectionListener {
            override fun onIndexSelected(index: Int) {
                val value = imageSelector.items[index].value!!
            }

            override fun onIndexDeselected(index: Int) {}
        })
    }

    private fun showPickItemForItemAtIndexDialog(index: Int, items: List<Item<List<GuidepostSport>>>) {
        val ctx = context ?: return
        ImageListPickerDialog(ctx, items, R.layout.cell_icon_select_with_label_below, 3) { selected ->
            val newList = imageSelector.items.toMutableList()
            newList[index] = selected
            imageSelector.items = newList
        }.show()
    }

    override fun onClickOk(selectedItems: List<List<GuidepostSport>>) {
        val answer = SelectedGuidepostSports(selectedItems.flatten())
        applyAnswer(answer)
    }

    private fun confirmJustSimple() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_guidepost_sports_answer_simple_description)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(IsSimpleGuidepost) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }
}
