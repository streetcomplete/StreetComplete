package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.ui.common.street_side_select.Side
import kotlinx.serialization.Serializable

@Serializable
data class Sides<T>(val left: T?, val right: T?)

fun <T> Sides<T>.any(block: (side: T?) -> Boolean): Boolean =
    block(left) || block(right)

fun <T> Sides<T>.all(block: (side: T?) -> Boolean): Boolean =
    block(left) && block(right)

operator fun <T> Sides<T>.get(side: Side): T? = when (side) {
    Side.LEFT -> left
    Side.RIGHT -> right
}
