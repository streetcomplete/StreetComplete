package de.westnordost.streetcomplete.ui.common.image_select

import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.Text
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem

data class Item<T>(
    override val value: T?,
    val drawableId: Int? = null,
    val titleId: Int? = null,
    val descriptionId: Int? = null,
    override val items: List<de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem<T>>? = null
) : GroupableDisplayItem<T> {

    override val image: Image? get() = drawableId?.let { ResImage(it) }
    override val title: Text? get() = titleId?.let { ResText(titleId) }
    override val description: Text? get() = descriptionId?.let { ResText(descriptionId) }
}

data class Item2<T>(
    override val value: T?,
    override val image: Image? = null,
    override val title: Text? = null,
    override val description: Text? = null
) : DisplayItem<T>
