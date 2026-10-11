package de.westnordost.streetcomplete.quests.recycling_material

fun parseRecyclingMaterials(tags: Map<String, String>): Set<RecyclingMaterial> {
    val initialSelectedItems = mutableSetOf<RecyclingMaterial>()
    for (type in RecyclingMaterial.entries) {
        if (tags["recycling:${type.value}"] == "yes") {
            initialSelectedItems += type
        }
    }
    return initialSelectedItems
}
