package de.westnordost.streetcomplete.quests

import android.content.res.Resources
import android.text.Html
import androidx.core.text.parseAsHtml
import de.westnordost.streetcomplete.R

/** Returns the label text that shortly describes where the element with the given tags is located */
fun Resources.getLocationLabelString(tags: Map<String, String>): CharSequence? {
    // prefer to show the level if both are present because it is a more precise indication
    // where it is supposed to be
    return getLevelLabelString(tags) ?: getHouseNumberLabelString(tags)
}

private fun Resources.getLevelLabelString(tags: Map<String, String>): CharSequence? {
    /* distinguish between "floor" and "level":
       E.g. addr:floor may be "M" while level is "2". The "2" is in this case purely technical and
       can not be seen on any sign. */
    val floor = tags["addr:floor"] ?: tags["level:ref"]
    if (floor != null) {
        return getString(R.string.on_floor, floor)
    }
    val level = tags["level"]
    if (level != null) {
        return getString(R.string.on_level, level)
    }
    if (tags["tunnel"] == "yes" || tags["location"] == "underground") {
        return getString(R.string.underground)
    }
    return null
}

private fun Resources.getHouseNumberLabelString(tags: Map<String, String>): CharSequence? {
    val houseName = tags["addr:housename"]
    val conscriptionNumber = tags["addr:conscriptionnumber"]
    val streetNumber = tags["addr:streetnumber"]
    val houseNumber = tags["addr:housenumber"]

    if (houseName != null) {
        return getString(R.string.at_housename, "<i>" + Html.escapeHtml(houseName) + "</i>").parseAsHtml()
    }
    if (conscriptionNumber != null) {
        if (streetNumber != null) {
            return getString(R.string.at_conscription_and_street_number, conscriptionNumber, streetNumber)
        } else {
            return getString(R.string.at_conscription_number, conscriptionNumber)
        }
    }
    if (houseNumber != null) {
        return getString(R.string.at_housenumber, houseNumber)
    }
    return null
}
