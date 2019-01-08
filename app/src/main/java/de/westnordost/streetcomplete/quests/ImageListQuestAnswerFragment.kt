package de.westnordost.streetcomplete.quests

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import android.view.View
import androidx.core.os.bundleOf

import java.util.ArrayList
import java.util.LinkedList

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.GroupedItem
import de.westnordost.streetcomplete.view.ImageSelectAdapter
import kotlinx.android.synthetic.main.quest_generic_list.*

/**
 * Abstract class for quests with a list of images and one or several to select.
 */
abstract class ImageListQuestAnswerFragment : AbstractQuestFormAnswerFragment() {

    override val contentLayoutResId = R.layout.quest_generic_list

    protected lateinit var imageSelector: ImageSelectAdapter

    @Inject internal lateinit var favs: LastPickedValuesStore

    protected open val itemsPerRow = 4
    /** return -1 for any number. Default: 1  */
    protected open val maxSelectableItems = 1
    /** return -1 for showing all items at once. Default: -1  */
    protected open val maxNumberOfInitiallyShownItems = -1

    protected abstract val items: List<GroupedItem>

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector = ImageSelectAdapter(maxSelectableItems)
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

        showMoreButton.setOnClickListener {
            imageSelector.items = items.favouritesMovedToFront()
            showMoreButton.visibility = View.GONE
        }

        var initiallyShow = maxNumberOfInitiallyShownItems
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(EXPANDED)) initiallyShow = -1
            showItems(initiallyShow)

            val selectedIndices = savedInstanceState.getIntegerArrayList(SELECTED_INDICES)!!
            imageSelector.select(selectedIndices)
        } else {
            showItems(initiallyShow)
        }
        list.adapter = imageSelector
    }

    override fun onClickOk() {
        applyAnswer()
    }

    protected fun applyAnswer() {
        val osmValues = imageSelector.selectedItems.map { it.value!! }
        if (osmValues.isNotEmpty()) {
            favs.add(javaClass.simpleName, osmValues)
            applyAnswer(bundleOf(OSM_VALUES to ArrayList(osmValues)))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntegerArrayList(SELECTED_INDICES, ArrayList(imageSelector.selectedIndices))
        outState.putBoolean(EXPANDED, showMoreButton.visibility == View.GONE)
    }

    override fun isFormComplete() = imageSelector.selectedIndices.isNotEmpty()

    private fun showItems(initiallyShow: Int) {
        val allItems = items
        val showAll = initiallyShow == -1 || initiallyShow >= allItems.size

        showMoreButton.visibility = if(showAll) View.GONE else View.VISIBLE
        val sortedItems = allItems.favouritesMovedToFront()
        imageSelector.items = if(showAll) sortedItems else sortedItems.subList(0, initiallyShow)
    }

    private fun List<GroupedItem>.favouritesMovedToFront(): List<GroupedItem> {
        val result: LinkedList<GroupedItem> = LinkedList(this)

        if (result.size > itemsPerRow) {
            favs.moveLastPickedToFront(javaClass.simpleName, result, this)
        }
        return result
    }

    companion object {

        const val OSM_VALUES = "osm_values"

        private const val SELECTED_INDICES = "selected_indices"
        private const val EXPANDED = "expanded"
    }
}
