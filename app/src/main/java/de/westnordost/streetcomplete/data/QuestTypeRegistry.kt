package de.westnordost.streetcomplete.data

/** Every osm quest needs to be registered here.
 *
 * Could theoretically be done with Reflection, but that doesn't really work on Android
 */

class QuestTypeRegistry(val all: List<QuestType<*>>) {
    private val typeMap: Map<String, QuestType<*>>

    init {
        val map = mutableMapOf<String, QuestType<*>>()
        for (questType in all) {
            val questTypeName = questType.javaClass.simpleName
            require(!map.containsKey(questTypeName)) {
                "A quest type's name must be unique! \"$questTypeName\" is defined twice!"
            }
            map[questTypeName] = questType
        }
        typeMap = map
    }

    fun getByName(typeName: String): QuestType<*>? {
        return typeMap[typeName]
    }
}
