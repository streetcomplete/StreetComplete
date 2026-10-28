package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.util.ktx.getOrPutDefaultIfMissing
import de.westnordost.streetcomplete.util.ktx.getOrPutIfMissing
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
fun reverseMapOf(merge: List<Pair<String, String>>) : Map<String, List<String>> {
    val result = mutableMapOf<String, MutableList<String>>().withDefault { mutableListOf(it) }
    merge.forEach {
        result.getOrPutDefaultIfMissing(it.second).add(it.first)
    }
    return result
}

// Return a list, where keys are merged together according to `forward`.
fun merge(list: List<EditTypeStatistics>, forward: Map<String, String>) : List<EditTypeStatistics> {
    val result = mutableMapOf<String, Int>().withDefault { 0 }
    list.forEach {
        val mappedKey = forward.getValue(it.type)
        result[mappedKey] = result.getValue(mappedKey) + it.count
    }
    return result.map { EditTypeStatistics(it.key, it.value) }.toList()
}

/**
 * Wrap a [StatisticsSource], but merge [Pair.first] into [Pair.second] for
 * each of the pairs given in [merge].
 */
class MergedStatistics(private val inner: StatisticsSource, merge: List<Pair<String, String>>) : StatisticsSource by inner {
    private class Listener(private val inner: StatisticsSource.Listener, private val forward: Map<String, String>) : StatisticsSource.Listener by inner {
        override fun onAddedOne(type: String) {
            inner.onAddedOne(forward.getValue(type))
        }
        override fun onSubtractedOne(type: String) {
            inner.onSubtractedOne(forward.getValue(type))
        }
    }

    val forward = merge.toMap().withDefault { it }
    val backward = reverseMapOf(merge)
    val wrappedListenersLock = ReentrantLock()
    val wrappedListeners = mutableMapOf<StatisticsSource.Listener, StatisticsSource.Listener>()

    override fun getEditCount(type: String): Int {
        return backward.getValue(type).sumOf { inner.getEditCount(it) }
    }

    override fun getEditCount(types: List<String>): Int {
        return inner.getEditCount(types.flatMap { backward.getValue(it) })
    }

    override fun getEditTypeStatistics(): List<EditTypeStatistics> {
        return merge(inner.getEditTypeStatistics(), forward)
    }

    override fun getCurrentWeekEditTypeStatistics(): List<EditTypeStatistics> {
        return merge(inner.getCurrentWeekEditTypeStatistics(), forward)
    }

    override fun addListener(listener: StatisticsSource.Listener) {
        wrappedListenersLock.withLock {
            inner.addListener(wrappedListeners.getOrPutIfMissing(listener, { Listener(listener, forward) }))
        }
    }

    override fun removeListener(listener: StatisticsSource.Listener) {
        wrappedListenersLock.withLock {
            wrappedListeners.remove(listener)?.let {
                inner.removeListener(it)
            }
        }
    }
}
