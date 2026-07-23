package de.westnordost.streetcomplete.quests.leaf_detail

import kotlinx.serialization.Serializable

@Serializable
enum class ForestLeafType(val osmValue: String) {
    NEEDLELEAVED("needleleaved"),
    BROADLEAVED("broadleaved"),
    MIXED("mixed"),
}
