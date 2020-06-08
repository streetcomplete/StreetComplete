package de.westnordost.streetcomplete.quests

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import android.view.View

import java.util.ArrayList
import java.util.LinkedList

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.ImageSelectAdapter
import de.westnordost.streetcomplete.view.Item
import kotlinx.android.synthetic.main.quest_generic_list.*

/**
 * Abstract class for quests with a list of images and one or several to select.
 *
 * I is the type of each item in the image list (a simple model object). In MVC, this would
 * be the view model.
 *
 * T is the type of the answer object (also a simple model object) created by the quest
 * form and consumed by the quest type. In MVC, this would be the model.
 */
abstract class AImageListQuestAnswerFragment<I,T> : AbstractQuestFormAnswerFragment<T>() {

    override val contentLayoutResId = R.layout.quest_generic_list

    protected lateinit var imageSelector: ImageSelectAdapter<I>

    private lateinit var favs: LastPickedValuesStore<I>

    protected open val itemsPerRow = 4
    /** return -1 for any number. Default: 1  */
    protected open val maxSelectableItems = 1
    /** return true to move last picked items to the front. On by default. Only respected if the
     *  items do not all fit into one line */
    protected open val moveFavoritesToFront = true

    protected abstract val items: List<Item<I>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector = ImageSelectAdapter(maxSelectableItems)
    }

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.layoutManager = GridLayoutManager(activity, itemsPerRow)
        list.isNestedScrollingEnabled = false

        selectHintLabel.setText(if (maxSelectableItems == 1) R.string.quest_roofShape_select_one else R.string.quest_select_hint)

        imageSelector.listeners.add(object : ImageSelectAdapter.OnItemSelectionListener {
            override fun onIndexSelected(index: Int) {
                checkIsFormComplete()
            }

            override fun onIndexDeselected(index: Int) {
                checkIsFormComplete()
            }
        })

        showMoreButton.visibility = View.GONE
        
        imageSelector.items = moveFavouritesToFront(items)
        if (savedInstanceState != null) {
            val selectedIndices = savedInstanceState.getIntegerArrayList(SELECTED_INDICES)!!
            imageSelector.select(selectedIndices)
        }
        list.adapter = imageSelector
    }

    override fun onClickOk() {
        val values = imageSelector.selectedItems
        if (values.isNotEmpty()) {
            favs.add(javaClass.simpleName, values)
            onClickOk(values)
        }
    }

    protected abstract fun onClickOk(selectedItems: List<I>)

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // note: the view might be not available anymore at this point!
        outState.putIntegerArrayList(SELECTED_INDICES, ArrayList(imageSelector.selectedIndices))
    }

    override fun isFormComplete() = imageSelector.selectedIndices.isNotEmpty()

    private fun moveFavouritesToFront(originalList: List<Item<I>>): List<Item<I>> {
        val result: LinkedList<Item<I>> = LinkedList(originalList)

        if (result.size > itemsPerRow && moveFavoritesToFront) {
            favs.moveLastPickedToFront(javaClass.simpleName, result, originalList)
        }
        return result
    }

    companion object {
        private const val SELECTED_INDICES = "selected_indices"
    }
}
