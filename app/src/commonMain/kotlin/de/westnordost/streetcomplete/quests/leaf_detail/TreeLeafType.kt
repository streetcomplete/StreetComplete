package de.westnordost.streetcomplete.quests.leaf_detail

sealed interface TreeLeafTypeAnswer

enum class TreeLeafType(val osmValue: String) : TreeLeafTypeAnswer {
    NEEDLELEAVED("needleleaved"),
    BROADLEAVED("broadleaved"),
}

data object NotTreeButStump : TreeLeafTypeAnswer
