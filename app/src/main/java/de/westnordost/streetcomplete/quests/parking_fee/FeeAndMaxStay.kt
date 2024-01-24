package de.westnordost.streetcomplete.quests.parking_fee

import de.westnordost.streetcomplete.osm.Tags

data class FeeAndMaxStay(val fee: Fee, val maxstay: MaxStay? = null)

fun FeeAndMaxStay.applyTo(tags: Tags) {
    fee.applyTo(tags)
    maxstay?.applyTo(tags)
}
