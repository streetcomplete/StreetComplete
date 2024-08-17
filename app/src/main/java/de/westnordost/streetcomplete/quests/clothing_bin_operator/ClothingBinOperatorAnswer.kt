package de.westnordost.streetcomplete.quests.clothing_bin_operator

sealed interface ClothingBinOperatorAnswer

data object NoClothingBinOperatorSigned :ClothingBinOperatorAnswer
data class ClothingBinOperator(val name: String): ClothingBinOperatorAnswer
