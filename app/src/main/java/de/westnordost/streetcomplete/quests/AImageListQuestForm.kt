package de.westnordost.streetcomplete.quests

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.recyclerview.widget.GridLayoutManager
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestGenericListBinding
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.padWith
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageSelectAdapter
import org.koin.android.ext.android.inject

/**
 * Abstract class for quests with a list of images and one or several to select.
 *
 * I is the type of each item in the image list (a simple model object). In MVC, this would
 * be the view model.
 *
 * T is the type of the answer object (also a simple model object) created by the quest
 * form and consumed by the quest type. In MVC, this would be the model.
 */
abstract class AImageListQuestForm<I, T> : AbstractOsmQuestForm<T>() {

    final override val contentLayoutResId = R.layout.quest_generic_list
    private val binding by contentViewBinding(QuestGenericListBinding::bind)

    private val prefs: ObservableSettings by inject()

    override val defaultExpanded = false

    protected open val descriptionResId: Int? = null

    protected lateinit var imageSelector: ImageSelectAdapter<I>

    private lateinit var favs: LastPickedValuesStore<DisplayItem<I>>

    protected open val itemsPerRow = 4
    /** return -1 for any number. Default: 1  */
    protected open val maxSelectableItems = 1
    /** return true to move last picked items to the front. On by default. Only respected if the
     *  items do not all fit into one line */
    protected open val moveFavoritesToFront = true
    /** items to display. May not be accessed before onCreate */
    protected abstract val items: List<DisplayItem<I>>

    private lateinit var itemsByString: Map<String, DisplayItem<I>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector = ImageSelectAdapter(maxSelectableItems)
        itemsByString = items.associateBy { it.value.toString() }
    }

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            prefs,
            key = javaClass.simpleName,
            serialize = { it.value.toString() },
            deserialize = { itemsByString[it] }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.descriptionLabel.isGone = descriptionResId == null
        descriptionResId?.let { binding.descriptionLabel.setText(it) }

        binding.list.layoutManager = GridLayoutManager(activity, itemsPerRow)
        binding.list.isNestedScrollingEnabled = false

        binding.selectHintLabel.setText(if (maxSelectableItems == 1) R.string.quest_roofShape_select_one else R.string.quest_select_hint)

        imageSelector.listeners.add(object : ImageSelectAdapter.OnItemSelectionListener {
            override fun onIndexSelected(index: Int) {
                checkIsFormComplete()
            }

            override fun onIndexDeselected(index: Int) {
                checkIsFormComplete()
            }
        })

        imageSelector.items = moveFavouritesToFront(items)
        if (savedInstanceState != null) {
            val selectedIndices = savedInstanceState.getIntegerArrayList(SELECTED_INDICES)!!
            imageSelector.select(selectedIndices)
        }
        binding.list.adapter = imageSelector
    }

    override fun onClickOk() {
        val values = imageSelector.selectedItems
        if (values.isNotEmpty()) {
            favs.add(values)
            onClickOk(values.map { it.value!! })
        }
    }

    protected abstract fun onClickOk(selectedItems: List<I>)

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // note: the view might be not available anymore at this point!
        outState.putIntegerArrayList(SELECTED_INDICES, ArrayList(imageSelector.selectedIndices))
    }

    override fun isFormComplete() = imageSelector.selectedIndices.isNotEmpty()

    private fun moveFavouritesToFront(originalList: List<DisplayItem<I>>): List<DisplayItem<I>> =
        if (originalList.size > itemsPerRow && moveFavoritesToFront) {
            favs.get().filterNotNull().padWith(originalList).toList()
        } else {
            originalList
        }

    companion object {
        private const val SELECTED_INDICES = "selected_indices"
    }
}
