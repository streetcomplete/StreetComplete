package de.westnordost.streetcomplete.data.osm.elementgeometry


/** Knows which vertices connect which ways. T is the identifier of a vertex  */
class NodeWayMap<T>(ways: List<List<T>>) {
    private val wayEndpoints = LinkedHashMap<T, MutableList<List<T>>>()

    init {
        for (way in ways) {
            val firstNode = way.first()
            val lastNode = way.last()

            wayEndpoints.getOrPut(firstNode, { ArrayList() }).add(way)
            wayEndpoints.getOrPut(lastNode, { ArrayList() }).add(way)
        }
    }

    fun hasNextNode(): Boolean = wayEndpoints.isNotEmpty()

    fun getNextNode(): T = wayEndpoints.keys.iterator().next()

    fun getWaysAtNode(node: T): List<List<T>>? = wayEndpoints[node]

    fun removeWay(way: List<T>) {
        val it = wayEndpoints.values.iterator()
        while (it.hasNext()) {
            val waysPerNode = it.next()

            val waysIt = waysPerNode.iterator()
            while (waysIt.hasNext()) {
                if (waysIt.next() === way) {
                    waysIt.remove()
                }
            }

            if (waysPerNode.isEmpty()) {
                it.remove()
            }
        }
    }
}
