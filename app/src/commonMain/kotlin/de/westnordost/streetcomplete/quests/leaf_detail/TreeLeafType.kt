package de.westnordost.streetcomplete.quests.leaf_detail

import kotlinx.serialization.Serializable

sealed interface TreeLeafTypeAnswer

@Serializable
enum class TreeLeafType(val osmValue: String) : TreeLeafTypeAnswer {
    NEEDLELEAVED("needleleaved"),
    BROADLEAVED("broadleaved"),
}

data object NotTreeButStump : TreeLeafTypeAnswer
