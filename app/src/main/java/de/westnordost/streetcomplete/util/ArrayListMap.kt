package de.westnordost.streetcomplete.util

/** A "Map" that is actually a list. Hence, DO NOT USE this except if you know exactly what you are
 *  doing - the performance of any map-related operation except .entries will be O(n).
 *
 *  This class exists because some third-party interfaces expect a Map when the only thing they do
 *  with it is to iterate through its entries and afterwards throw it away. If that happens en-masse
 *  this is quite memory inefficient. */
class ListMap<K,V>(list: List<Pair<K,V>>) : Map<K,V> {
    override val entries: Set<Map.Entry<K, V>> = ListSet(list.map { Entry(it.first, it.second) })
    override val keys: Set<K> get() = ListSet(entries.map { it.key })
    override val size: Int = entries.size
    override val values: Collection<V> get() = entries.map { it.value }
    override fun containsKey(key: K): Boolean = entries.find { it.key == key } != null
    override fun containsValue(value: V): Boolean = entries.find { it.value == value } != null
    override fun get(key: K): V? = entries.find { it.key == key }?.value
    override fun isEmpty(): Boolean = size == 0

    private data class Entry<K,V>(override val key: K, override val value: V) : Map.Entry<K,V>

    private class ListSet<E>(private val list: List<E>) : Set<E> {
        override val size: Int get() = list.size
        override fun contains(element: E): Boolean = list.contains(element)
        override fun containsAll(elements: Collection<E>): Boolean = list.containsAll(elements)
        override fun isEmpty(): Boolean = size == 0
        override fun iterator(): Iterator<E> = list.iterator()
    }
}
