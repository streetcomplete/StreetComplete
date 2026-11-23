package de.westnordost.streetcomplete.ui.common.item_select

import kotlinx.serialization.Serializable

@Serializable
data class Group<C, I>(
    val category: C,
    val item: I?,
    val children: List<I>
)
