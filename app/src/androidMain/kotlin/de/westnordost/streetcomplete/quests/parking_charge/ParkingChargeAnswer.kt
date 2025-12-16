package de.westnordost.streetcomplete.quests.parking_charge

import de.westnordost.streetcomplete.osm.fee.Fee
import de.westnordost.streetcomplete.osm.maxstay.MaxStay

data class ParkingChargeAnswer(val fee: Fee, val maxstay: MaxStay? = null)
