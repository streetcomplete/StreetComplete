package de.westnordost.streetcomplete.quests.leaf_detail

sealed interface TreeLeafTypeAnswer

enum class TreeLeafType(val osmValue: String) : TreeLeafTypeAnswer {
    NEEDLELEAVED("needleleaved"),
    BROADLEAVED("broadleaved"),
    LEAFLESS("leafless"),
}

data object NotTreeButStump : TreeLeafTypeAnswer
data object DeadTree : TreeLeafTypeAnswer
