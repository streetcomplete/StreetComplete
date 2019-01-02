package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

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

    protected lateinit var imageSelector: ImageSelectAdapter

    @Inject internal lateinit var favs: LastPickedValuesStore

    protected open val itemsPerRow = 4
    /** return -1 for any number. Default: 1  */
    protected open val maxSelectableItems = 1
    /** return -1 for showing all items at once. Default: -1  */
    protected open val maxNumberOfInitiallyShownItems = -1

    protected abstract val items: List<GroupedItem>

    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        setContentView(R.layout.quest_generic_list)

        val lm = GridLayoutManager(activity, itemsPerRow)
        list.layoutManager = lm
        list.isNestedScrollingEnabled = false

        showMoreButton.setOnClickListener {
            imageSelector.items = items.favouritesMovedToFront()
            showMoreButton.visibility = View.GONE
        }

        val selectableItems = maxSelectableItems
        selectHintLabel.setText(if (selectableItems == 1) R.string.quest_roofShape_select_one else R.string.quest_select_hint)

        imageSelector = ImageSelectAdapter(selectableItems)
        imageSelector.listeners.add(object : ImageSelectAdapter.OnItemSelectionListener {
            override fun onIndexSelected(index: Int) {
                checkIsFormComplete()
            }

            override fun onIndexDeselected(index: Int) {
                checkIsFormComplete()
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var initiallyShow = maxNumberOfInitiallyShownItems
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(EXPANDED)) initiallyShow = -1
            showItems(initiallyShow)

            val selectedIndices = savedInstanceState.getIntegerArrayList(SELECTED_INDICES)
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
        val answer = Bundle()

        val osmValues = imageSelector.selectedItems.map { it.value!! }
        if (osmValues.isNotEmpty()) {
            answer.putStringArrayList(OSM_VALUES, ArrayList(osmValues))
            favs.add(javaClass.simpleName, osmValues)
            applyAnswer(answer)
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
