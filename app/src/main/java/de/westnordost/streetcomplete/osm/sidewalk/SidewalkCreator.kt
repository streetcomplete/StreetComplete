package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.expandSides
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.mergeSides
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.*
import de.westnordost.streetcomplete.osm.updateCheckDateForKey

fun LeftAndRightSidewalk.applyTo(tags: Tags) {
    if (left == null && right == null) return
    /* for being able to modify only one side (e.g. `left` is null while `right` is not null),
       first the conflated and merged sidewalk values (sidewalk=both and sidewalk:both=yes etc.)
       need to be separated.
       So even if there is an invalid value such as sidewalk=narrow but only the right side
       is modified to "yes" while the left side is not touched, it means that in the end, the
       invalid value must still be in the end result, like this:
       - sidewalk:left=narrow
       - sidewalk:right=yes
       First separating the values and then later conflating them again, if possible, solves this.
    */

    /* NOT expanding the bare tag, because the bare tag follows a different schema
       E.g. sidewalk=both  !=  sidewalk:left=both + sidewalk:right=both
       See separateConflatedSidewalk for proper parsing of that */
    tags.expandSides("sidewalk", includeBareTag = false)
    tags.separateConflatedSidewalk()

    if (left != null)  tags["sidewalk:left"] = left.osmValue
    if (right != null) tags["sidewalk:right"] = right.osmValue

    // use shortcut syntax if possible, preferred by community according to usage numbers on taginfo
    tags.conflateSidewalk()
    tags.mergeSides("sidewalk")

    if (!tags.hasChanges || tags.hasCheckDateForKey("sidewalk")) {
        tags.updateCheckDateForKey("sidewalk")
    }
}

/** converts sidewalk=both to sidewalk:left=yes + sidewalk:right=yes etc. */
private fun Tags.separateConflatedSidewalk() {
    val leftRight = getSeparatedSidewalkValues(get("sidewalk")) ?: return
    // don't overwrite more specific values if already set
    if (!containsKey("sidewalk:left")) set("sidewalk:left", leftRight.first)
    if (!containsKey("sidewalk:right")) set("sidewalk:right", leftRight.second)
    remove("sidewalk")
}

private fun getSeparatedSidewalkValues(sidewalk: String?): Pair<String, String>? = when (sidewalk) {
    "both" ->  Pair("yes", "yes")
    "left" ->  Pair("yes", "no")
    "right" -> Pair("no", "yes")
    null ->    null
    // for "separate", "no", etc., and also invalid values
    else ->    Pair(sidewalk, sidewalk)
}

/** converts sidewalk:left=yes + sidewalk:right=yes to sidewalk=both etc. */
private fun Tags.conflateSidewalk() {
    val sidewalk = getConflatedSidewalkValue(get("sidewalk:left"), get("sidewalk:right"))
    if (sidewalk != null) {
        remove("sidewalk:left")
        remove("sidewalk:right")
        set("sidewalk", sidewalk)
    }
}

private fun getConflatedSidewalkValue(left: String?, right: String?): String? = when {
    left == "yes" && right == "no" -> "left"
    left == "no" && right == "yes" -> "right"
    /* shall only work for "yes" and "no" because the wiki states that sidewalk:both=separate
       should be preferred over sidewalk=separate: The former is more explicit while for the latter,
       it is not entirely clear if it is meant for both sides or only one side.
       (Same as with cycleway=separate vs cycleway:both=separate) */
    left == right -> when (left) {
        "yes" -> "both"
        "no" ->  "no"
        else ->  null
    }
    else -> null
}

private val Sidewalk.osmValue: String get() = when (this) {
    YES -> "yes"
    NO -> "no"
    SEPARATE -> "separate"
    else -> {
        throw IllegalArgumentException("Attempting to tag invalid sidewalk")
    }
}
