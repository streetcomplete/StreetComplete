package de.westnordost.streetcomplete.data.osm.geometry.polygons

//Basic heap priorityQueue made to avoid java dependencies
class PriorityQueue<T : Comparable<T>>() {
    private val items = mutableListOf<T>()

    val isEmpty: Boolean get() = items.isEmpty()
    val size: Int get() = items.size

    fun add(element: T) {
        items.add(element)
        siftUp(items.lastIndex)
    }

    fun poll(): T {
        if(items.isEmpty()) throw NoSuchElementException("empty queue")
        val root = items[0]
        val last = items.removeAt(items.lastIndex)
        if(items.isNotEmpty()) {
            items[0] = last
            siftDown(0)
        }
        return root
    }

    private fun siftUp(index: Int){
        var i = index
        while(i > 0){
            val parent = (i-1)/2
            if (items[i] <= items[parent]) break
            items[i] = items[parent].also { items[parent] = items[i] }
            i = parent
        }
    }

    private fun siftDown(index: Int) {
        var i = index
        while(true){
            val left = 2 * i + 1
            val right = left + 1
            var largest = i
            if (left < items.size && items[left] > items[largest]) largest = left
            if (right < items.size && items[right] > items[largest]) largest = right
            if(largest == i) break
            items[i] = items[largest].also { items[largest] = items[i] }
            i = largest
        }
    }
}
