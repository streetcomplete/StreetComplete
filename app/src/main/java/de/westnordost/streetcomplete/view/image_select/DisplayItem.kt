package de.westnordost.streetcomplete.view.image_select

import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.Text

interface DisplayItem<T> {
    val value: T?
    val image: Image?
    val title: Text?
    val description: Text?
}

interface GroupableDisplayItem<T> : DisplayItem<T> {
    val items: List<GroupableDisplayItem<T>>?
    val isGroup: Boolean get() = !items.isNullOrEmpty()
}


