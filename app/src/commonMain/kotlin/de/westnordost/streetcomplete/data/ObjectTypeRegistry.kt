package de.westnordost.streetcomplete.data

/** A class where objects of a certain type are
 *  1. registered and can be recalled by class name
 *  2. or recalled by ordinal
 *  3. or iterated in the order as specified in the constructor
 */
open class ObjectTypeRegistry<T>(ordinalsAndEntries: List<Pair<Int, T & Any>>) : AbstractList<T>() {

    private val byName: Map<String, T>
    private val byOrdinal: Map<Int, T>
    private val ordinalByObject: Map<T, Int>
    private val objects = ordinalsAndEntries.map { it.second }

    init {
        val byNameMap = mutableMapOf<String, T>()
        val highestOrdinal = ordinalsAndEntries.maxBy { it.first }.first
        val byOrdinalMap = HashMap<Int, T>(highestOrdinal + 1)
        for ((ordinal, objectType) in ordinalsAndEntries) {
            val typeName = objectType::class.simpleName!!
            require(!byNameMap.containsKey(typeName)) {
                "A object type's name must be unique! \"$typeName\" is defined twice!"
            }
            require(!byOrdinalMap.containsKey(ordinal)) {
                val otherTypeName = byOrdinalMap[ordinal]!!::class.simpleName!!
                "Duplicate ordinal for \"$typeName\" and \"$otherTypeName\""
            }
            byNameMap[typeName] = objectType
            byOrdinalMap[ordinal] = objectType
        }
        ordinalByObject = ordinalsAndEntries.associate { it.second to it.first }
        byName = byNameMap
        byOrdinal = byOrdinalMap
    }

    fun getByName(typeName: String): T? = byName[typeName]

    fun getByOrdinal(ordinal: Int): T? = byOrdinal[ordinal]

    fun getOrdinalOf(type: T): Int? = ordinalByObject[type]

    override val size: Int get() = objects.size
    override fun get(index: Int): T = objects[index]
}
