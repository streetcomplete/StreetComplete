/**
 * A simple max-heap priority queue that stores elements implementing Comparable<T>.
 *
 * poll() returns and removes the largest element.
 * add() inserts a new element in O(log n).
 *
 * Internally this uses a binary heap stored in a MutableList.
 */
class PriorityQueue<T : Comparable<T>> {
    private val items = mutableListOf<T>()

    val isEmpty: Boolean get() = items.isEmpty()
    val size: Int get() = items.size

    fun add(element: T) {
        items.add(element)
        siftUp(items.lastIndex)
    }

    fun poll(): T {
        if (items.isEmpty()) throw NoSuchElementException("empty queue")
        val root = items[0]
        val last = items.removeAt(items.lastIndex)

        if (items.isNotEmpty()) {
            items[0] = last
            siftDown(0)
        }

        return root
    }

    fun isNotEmpty(): Boolean = items.size != 0

    private fun siftUp(index: Int) {
        var i = index
        while (i > 0) {
            val parent = (i - 1) / 2
            if (items[i] <= items[parent]) break

            val tmp = items[i]
            items[i] = items[parent]
            items[parent] = tmp

            i = parent
        }
    }

    private fun siftDown(index: Int) {
        var i = index
        val size = items.size

        while (true) {
            val left = 2 * i + 1
            val right = left + 1
            var largest = i

            if (left < size && items[left] > items[largest]) largest = left
            if (right < size && items[right] > items[largest]) largest = right
            if (largest == i) break

            val tmp = items[i]
            items[i] = items[largest]
            items[largest] = tmp

            i = largest
        }
    }
}
