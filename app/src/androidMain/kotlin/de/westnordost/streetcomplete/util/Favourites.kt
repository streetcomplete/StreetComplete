package de.westnordost.streetcomplete.util

/**
 * Returns up to [n] distinct items of this sequence which are composed in the following manner:
 *
 * - The first [first] distinct items unless they are `null`
 * - Then, (up to) the [n] most common distinct non-null items found in the first [history] items
 * - Finally, append the items in [pad]
 *
 * It is guaranteed that the resulting sequence is not longer than [n] and only consists of non-null
 * distinct items.
 */
fun <T : Any> List<T?>.takeFavourites(
    n: Int,
    history: Int = 50,
    first: Int = 0,
    pad: List<T> = emptyList()
): List<T> {
    val firstItems = take(first).filterNotNull()
    val mostCommonItems = mostCommonNonNullWithin(n, history)
    return (firstItems + mostCommonItems + pad).distinct().take(n)
}

/** Returns up to the [n] most common distinct non-null items within the first [history] number of
 *  items, ordered by their first occurrence in this sequence. */
private fun <T : Any> List<T?>.mostCommonNonNullWithin(n: Int, history: Int): List<T> {
    val counts = countUniqueNonNull(n, history)
    return counts.keys
        .sortedByDescending { counts[it]!!.count }
        .take(n)
        .sortedBy { counts[it]!!.indexOfFirst }
}

private data class ItemStats(val indexOfFirst: Int, var count: Int = 0)

/** Counts at least the first [count], keeps going until it finds at least [n] unique values */
private fun <T : Any> List<T?>.countUniqueNonNull(n: Int, count: Int): Map<T, ItemStats> {
    val counts = LinkedHashMap<T, ItemStats>()

    for (item in this.withIndex()) {
        if (item.index >= count && counts.size >= n) break
        item.value?.let { value ->
            counts.getOrPut(value) { ItemStats(item.index) }.count++
        }
    }

    return counts
}
