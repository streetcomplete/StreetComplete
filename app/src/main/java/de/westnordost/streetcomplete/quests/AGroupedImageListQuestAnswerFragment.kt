package de.westnordost.streetcomplete.quests

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import androidx.core.view.postDelayed

import javax.inject.Inject

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.GroupedImageSelectAdapter
import de.westnordost.streetcomplete.view.Item
import kotlinx.android.synthetic.main.fragment_quest_answer.*
import kotlinx.android.synthetic.main.quest_generic_list.*
import java.util.*

/**
 * Abstract class for quests with a grouped list of images and one to select.
 *
 * Saving and restoring state is not implemented
 */
abstract class AGroupedImageListQuestAnswerFragment<I,T> : AbstractQuestFormAnswerFragment<T>() {

    override val contentLayoutResId = R.layout.quest_generic_list

    protected lateinit var imageSelector: GroupedImageSelectAdapter<I>

    protected abstract val allItems: List<Item<I>>
    protected abstract val topItems: List<Item<I>>

    @Inject internal lateinit var favs: LastPickedValuesStore<I>

    private val selectedItem get() = imageSelector.selectedItem

    protected open val itemsPerRow = 3

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector = GroupedImageSelectAdapter(GridLayoutManager(activity, itemsPerRow))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.layoutManager = imageSelector.gridLayoutManager
        list.isNestedScrollingEnabled = false

        showMoreButton.setOnClickListener {
            imageSelector.items = allItems
            showMoreButton.visibility = View.GONE
        }

        selectHintLabel.setText(R.string.quest_select_hint_most_specific)

        imageSelector.listeners.add { checkIsFormComplete() }
        imageSelector.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                scrollTo(positionStart)
            }
        })
        checkIsFormComplete()

        imageSelector.items = getInitialItems()
        list.adapter = imageSelector
    }

    private fun scrollTo(index: Int) {
        val item = imageSelector.gridLayoutManager.getChildAt(Math.max(0, index - 1))
        if (item != null) {
            val itemPos = IntArray(2)
            item.getLocationInWindow(itemPos)
            val scrollViewPos = IntArray(2)
            scrollView.getLocationInWindow(scrollViewPos)

            scrollView.postDelayed(250) {
                scrollView.smoothScrollTo(0, itemPos[1] - scrollViewPos[1])
            }
        }
    }

    private fun getInitialItems(): List<Item<I>> {
        val items = LinkedList(topItems)
        favs.moveLastPickedToFront(javaClass.simpleName, items, allItems)
        return items
    }

    override fun onClickOk() {
        val item = selectedItem!!
        val itemValue = item.value

        if (itemValue == null) {
            AlertDialog.Builder(context!!)
                .setMessage(R.string.quest_generic_item_invalid_value)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        } else {
            if (item.isGroup) {
                AlertDialog.Builder(context!!)
                    .setMessage(R.string.quest_generic_item_confirmation)
                    .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                    .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                        favs.add(javaClass.simpleName, itemValue)
                        onClickOk(item.value)
                    }
                    .show()
            }
            else {
                favs.add(javaClass.simpleName, itemValue)
                onClickOk(item.value)
            }
        }
    }

    abstract fun onClickOk(value: I)

    override fun isFormComplete() = selectedItem != null
}
