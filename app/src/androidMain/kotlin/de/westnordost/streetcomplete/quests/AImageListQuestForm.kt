package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.QuestGenericListBinding
import de.westnordost.streetcomplete.util.takeFavorites
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

    private val prefs: Preferences by inject()

    override val defaultExpanded = false

    protected open val descriptionResId: Int? = null

    protected open val itemsPerRow = 4
    /** return -1 for any number. Default: 1  */
    protected open val maxSelectableItems = 1
    /** return true to move last picked items to the front. On by default. Only respected if the
     *  items do not all fit into one line */
    protected open val moveFavoritesToFront = true
    /** items to display. May not be accessed before onCreate */
    protected abstract val items: List<I>

    private lateinit var itemsByString: Map<String, I>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemsByString = items.associateBy { it.toString() }
    }

    @Composable protected abstract fun BoxScope.ItemContent(item: I)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.descriptionLabel.isGone = descriptionResId == null
        descriptionResId?.let { binding.descriptionLabel.setText(it) }

        binding.selectHintLabel.setText(
            if (maxSelectableItems == 1) R.string.quest_roofShape_select_one
            else R.string.quest_multiselect_hint
        )

        // TODO moveFavouritesToFront(items)
    }

    override fun onClickOk() {
        val values = listOf<I>() // TODO
        if (values.isNotEmpty()) {
            prefs.addLastPicked(this::class.simpleName!!, values.map { it.toString() })
            onClickOk(values.map { it })
        }
    }

    protected abstract fun onClickOk(selectedItems: List<I>)

    override fun isFormComplete() = false // TODO imageSelector.selectedIndices.isNotEmpty()

    private fun moveFavouritesToFront(originalList: List<I>): List<I> {
        if (originalList.size > itemsPerRow && moveFavoritesToFront) {
            val favourites = prefs.getLastPicked(this::class.simpleName!!)
                .map { itemsByString[it] }
                .takeFavorites(n = itemsPerRow, history = 50)
            return (favourites + originalList).distinct()
        } else {
            return originalList
        }
    }
}
