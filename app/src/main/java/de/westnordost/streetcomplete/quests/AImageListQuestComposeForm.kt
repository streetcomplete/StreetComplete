package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.compose.material.Surface
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.ui.common.image_select.DisplayItem
import de.westnordost.streetcomplete.ui.common.image_select.ImageList
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.view.image_select.Item
import org.koin.android.ext.android.inject

abstract class AImageListQuestComposeForm<I, T> : AbstractOsmQuestForm<T>() {
    final override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
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
    protected abstract val items: List<Item<I>>

    private lateinit var itemsByString: Map<String, DisplayItem<I>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // itemsByString = items.associateBy { it.value.toString() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeViewBase.content {
            Surface {
                ImageList(imageItems = items, itemsPerRow = itemsPerRow)
            }
        }
    }
}
