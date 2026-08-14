package de.westnordost.streetcomplete.quests.clothing_bin_operator

sealed interface ClothingBinOperatorAnswer {
    data object NoneSigned : ClothingBinOperatorAnswer
}

data class ClothingBinOperator(val name: String) : ClothingBinOperatorAnswer
