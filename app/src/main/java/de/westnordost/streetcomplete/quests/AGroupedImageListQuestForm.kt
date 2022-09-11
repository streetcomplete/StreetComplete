package de.westnordost.streetcomplete.quests

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.postDelayed
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestGenericListBinding
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.mostCommonWithin
import de.westnordost.streetcomplete.util.padWith
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.GroupedImageSelectAdapter

/**
 * Abstract class for quests with a grouped list of images and one to select.
 *
 * Saving and restoring state is not implemented
 */
abstract class AGroupedImageListQuestForm<I, T> : AbstractOsmQuestForm<T>() {

    final override val contentLayoutResId = R.layout.quest_generic_list
    private val binding by contentViewBinding(QuestGenericListBinding::bind)

    override val defaultExpanded = false

    protected lateinit var imageSelector: GroupedImageSelectAdapter<I>

    /** all items to display (after user pressed "see more"). May not be accessed before onCreate */
    protected abstract val allItems: List<GroupableDisplayItem<I>>
    /** initial items to display. May not be accessed before onCreate */
    protected abstract val topItems: List<GroupableDisplayItem<I>>

    private lateinit var favs: LastPickedValuesStore<GroupableDisplayItem<I>>

    private val selectedItem get() = imageSelector.selectedItem

    protected open val itemsPerRow = 3

    private lateinit var itemsByString: Map<String, GroupableDisplayItem<I>>

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext),
            key = javaClass.simpleName,
            serialize = { it.value.toString() },
            deserialize = { itemsByString[it] }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector = GroupedImageSelectAdapter(GridLayoutManager(activity, itemsPerRow))
        itemsByString = allItems.mapNotNull { it.items }.flatten().associateBy { it.value.toString() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.list.layoutManager = imageSelector.gridLayoutManager
        binding.list.isNestedScrollingEnabled = false

        binding.selectHintLabel.setText(R.string.quest_select_hint_most_specific)

        imageSelector.listeners.add { checkIsFormComplete() }
        imageSelector.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                scrollTo(positionStart - 1)
            }
        })
        checkIsFormComplete()

        imageSelector.items = getInitialItems() + allItems

        binding.list.adapter = imageSelector
    }

    private fun scrollTo(index: Int) {
        val item = imageSelector.gridLayoutManager.findViewByPosition(index) ?: return
        val itemPos = IntArray(2)
        item.getLocationInWindow(itemPos)
        val scrollViewPos = IntArray(2)
        scrollView.getLocationInWindow(scrollViewPos)

        scrollView.postDelayed(250) {
            scrollView.smoothScrollTo(0, itemPos[1] - scrollViewPos[1])
        }
    }

    private fun getInitialItems(): List<GroupableDisplayItem<I>> =
        favs.get().mostCommonWithin(6, historyCount = 50, first = 1).padWith(topItems).toList()

    override fun onClickOk() {
        val item = selectedItem!!
        val itemValue = item.value

        if (itemValue == null) {
            context?.let {
                AlertDialog.Builder(it)
                    .setMessage(R.string.quest_generic_item_invalid_value)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        } else {
            if (item.isGroup) {
                context?.let {
                    AlertDialog.Builder(it)
                        .setMessage(R.string.quest_generic_item_confirmation)
                        .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                        .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                            favs.add(item)
                            onClickOk(itemValue)
                        }
                        .show()
                }
            } else {
                favs.add(item)
                onClickOk(itemValue)
            }
        }
    }

    abstract fun onClickOk(value: I)

    override fun isFormComplete() = selectedItem != null
}
