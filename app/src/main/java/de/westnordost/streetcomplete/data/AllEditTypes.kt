package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.util.Mockable

@Mockable
class AllEditTypes(
    registries: List<ObjectTypeRegistry<out EditType>>
) : AbstractCollection<EditType>() {

    private val byName = registries.flatten().associateByTo(LinkedHashMap()) { it.name }

    override val size: Int get() = byName.size

    override fun iterator(): Iterator<EditType> = byName.values.iterator()

    fun getByName(typeName: String): EditType? = byName[typeName]
}
