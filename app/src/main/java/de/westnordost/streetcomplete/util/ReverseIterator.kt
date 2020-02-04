package de.westnordost.streetcomplete.util

class ReverseIterator<T>(list: MutableList<T>) : MutableIterator<T> {
    private val iterator: MutableListIterator<T> = list.listIterator(list.size)
	override fun hasNext(): Boolean = iterator.hasPrevious()
    override fun next(): T = iterator.previous()
    override fun remove() { iterator.remove() }
}
