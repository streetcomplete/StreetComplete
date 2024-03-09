package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.data.osm.edits.EditType

class AllEditTypes(
    registries: List<ObjectTypeRegistry<out EditType>>,
    extras: List<EditType> = emptyList()
) : AbstractCollection<EditType>() {

    private val byName = registries.flatten().associateByTo(LinkedHashMap()) { it.name }
        .apply { extras.forEach { put(it.name, it) } }

    override val size: Int get() = byName.size

    override fun iterator(): Iterator<EditType> = byName.values.iterator()

    fun getByName(typeName: String): EditType? = byName[typeName]
}
