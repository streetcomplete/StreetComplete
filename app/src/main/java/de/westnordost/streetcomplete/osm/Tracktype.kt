package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.lit.LitStatus
import de.westnordost.streetcomplete.osm.lit.createLitStatus
import de.westnordost.streetcomplete.view.image_select.Item

enum class Tracktype(val osmValue: String) {
    GRADE1("grade1"),
    GRADE2("grade2"),
    GRADE3("grade3"),
    GRADE4("grade4"),
    GRADE5("grade5");

    fun asItem() = when (this) {
        GRADE1 -> Item(GRADE1, R.drawable.tracktype_grade1, R.string.quest_tracktype_grade1)
        GRADE2 -> Item(GRADE2, R.drawable.tracktype_grade2, R.string.quest_tracktype_grade2a)
        GRADE3 -> Item(GRADE3, R.drawable.tracktype_grade3, R.string.quest_tracktype_grade3a)
        GRADE4 -> Item(GRADE4, R.drawable.tracktype_grade4, R.string.quest_tracktype_grade4)
        GRADE5 -> Item(GRADE5, R.drawable.tracktype_grade5, R.string.quest_tracktype_grade5)
        }

    companion object {
        fun items(): List<Item<Tracktype>> {
            return values().map { it.asItem() }
        }
    }
}

fun createTracktypeStatus(tags: Map<String, String>): Tracktype? = when (tags["tracktype"]) {
    "grade1" -> Tracktype.GRADE1
    "grade2" -> Tracktype.GRADE2
    "grade3" -> Tracktype.GRADE3
    "grade4" -> Tracktype.GRADE4
    "grade5" -> Tracktype.GRADE5
    else -> null // other tracktype values are invalid and may be treated as unset
}

fun Tracktype.applyTo(tags: Tags) {
    tags.updateWithCheckDate("tracktype", this.osmValue)
}

/*
    alternative, does not seem better:

    if ("tracktype" !in tags) {
        return null
    }
    try {
        return Tracktype.valueOf(tags["tracktype"]!!)
    } catch (e: IllegalArgumentException) {
        return null
    }
 */
