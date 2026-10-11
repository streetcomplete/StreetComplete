package de.westnordost.streetcomplete.quests.recycling_material

import de.westnordost.streetcomplete.util.tree.Node

fun parseRecyclingMaterials(tags: Map<String, String>): Set<RecyclingMaterial> {
    val initialSelectedItems = mutableSetOf<RecyclingMaterial>()
    for (type in RecyclingMaterial.entries) {
        if (tags["recycling:${type.value}"] == "yes") {
            initialSelectedItems += type
        }
    }
    for (node in RecyclingMaterial.tree.children) {
        removeDuplicateMaterials(node, initialSelectedItems, false)
    }
    return initialSelectedItems
}

private fun removeDuplicateMaterials(
    node: Node<RecyclingMaterial>,
    selectedItems: MutableSet<RecyclingMaterial>,
    alreadyIncluded: Boolean
) {
    val material = node.value
    val isPresent = material != null && material in selectedItems
    if (alreadyIncluded && isPresent) {
        selectedItems.remove(material)
    }
    val nextAlreadyIncluded = alreadyIncluded || isPresent
    for (child in node.children) {
        removeDuplicateMaterials(child, selectedItems, nextAlreadyIncluded)
    }
}
