package de.westnordost.streetcomplete.quests.parking_fee

import de.westnordost.streetcomplete.osm.fee.Fee
import de.westnordost.streetcomplete.osm.maxstay.MaxStay

data class ParkingFeeAnswer(val fee: Fee, val maxstay: MaxStay? = null)
