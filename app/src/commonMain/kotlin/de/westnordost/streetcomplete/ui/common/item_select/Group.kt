package de.westnordost.streetcomplete.ui.common.item_select

interface Group<I> {
    val item: I?
    val children: List<I>
}
