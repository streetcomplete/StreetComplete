package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.widget.NestedScrollView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.GroupedImageSelectAdapter
import de.westnordost.streetcomplete.view.GroupedItem
import kotlinx.android.synthetic.main.quest_generic_list.*
import java.util.*

/**
 * Abstract class for quests with a grouped list of images and one to select.
 *
 * Saving and restoring state is not implemented
 */
abstract class GroupedImageListQuestAnswerFragment : AbstractQuestFormAnswerFragment() {

    override val contentLayoutResId = R.layout.quest_generic_list

    private val uiThread = Handler(Looper.getMainLooper())

    protected lateinit var imageSelector: GroupedImageSelectAdapter

    private lateinit var scrollView: NestedScrollView

    protected abstract val allItems: List<GroupedItem>
    protected abstract val topItems: List<GroupedItem>

    @Inject internal lateinit var favs: LastPickedValuesStore

    private val selectedItem get() = imageSelector.selectedItem

    protected open val itemsPerRow = 3

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector = GroupedImageSelectAdapter(
            GridLayoutManager(
                activity,
                itemsPerRow
            )
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        scrollView = view!!.findViewById(R.id.scrollView) // TODO...

        return view
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

                val item = imageSelector.gridLayoutManager.getChildAt(Math.max(0, positionStart - 1))
                if (item != null) {
                    val itemPos = IntArray(2)
                    item.getLocationInWindow(itemPos)
                    val scrollViewPos = IntArray(2)
                    scrollView.getLocationInWindow(scrollViewPos)

                    uiThread.postDelayed({
                        scrollView.smoothScrollTo(0, itemPos[1] - scrollViewPos[1])
                    }, 250)
                }
            }
        })
        checkIsFormComplete()

        imageSelector.items = getInitialItems()
        list.adapter = imageSelector
    }

    private fun getInitialItems(): List<GroupedItem> {
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
                .setPositiveButton(R.string.ok, null)
                .show()
        } else {
            if (item.isGroup) {
                AlertDialog.Builder(context!!)
                    .setMessage(R.string.quest_generic_item_confirmation)
                    .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                    .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                        applyAnswerAndSave(itemValue)
                    }
                    .show()
            }
            else {
                applyAnswerAndSave(itemValue)
            }
        }
    }

    private fun applyAnswerAndSave(value: String) {
        favs.add(javaClass.simpleName, value)
        applyAnswer(value)
    }

    protected open fun applyAnswer(value: String) {
        val answer = Bundle()
        answer.putString(OSM_VALUE, value)
        applyAnswer(answer)
    }

    override fun isFormComplete() = selectedItem != null

    companion object {
        const val OSM_VALUE = "osm_value"
    }
}
