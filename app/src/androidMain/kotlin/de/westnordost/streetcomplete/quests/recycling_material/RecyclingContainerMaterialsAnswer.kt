package de.westnordost.streetcomplete.quests.recycling_material

sealed interface RecyclingContainerMaterialsAnswer

data object IsWasteContainer : RecyclingContainerMaterialsAnswer
data class RecyclingMaterials(val materials: List<RecyclingMaterial>) : RecyclingContainerMaterialsAnswer
