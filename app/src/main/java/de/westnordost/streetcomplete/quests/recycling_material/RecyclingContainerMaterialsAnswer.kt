package de.westnordost.streetcomplete.quests.recycling_material

sealed class RecyclingContainerMaterialsAnswer

object IsWasteContainer : RecyclingContainerMaterialsAnswer()
data class RecyclingMaterials(val materials: List<String>) : RecyclingContainerMaterialsAnswer()
