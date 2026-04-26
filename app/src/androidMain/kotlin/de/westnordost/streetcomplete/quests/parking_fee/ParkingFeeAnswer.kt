package de.westnordost.streetcomplete.quests.parking_fee

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.fee.Fee
import de.westnordost.streetcomplete.osm.fee.applyTo
import de.westnordost.streetcomplete.osm.maxstay.MaxStay
import de.westnordost.streetcomplete.osm.maxstay.applyTo
import kotlinx.serialization.Serializable

@Serializable
sealed interface ParkingFeeAnswer {
    fun isComplete(): Boolean

    @Serializable
    data class NoFeeButMaxStay(val maxstay: MaxStay) : ParkingFeeAnswer {
        override fun isComplete(): Boolean = maxstay.isComplete()
    }
}

@Serializable
data class ParkingFee(val fee: Fee) : ParkingFeeAnswer {
    override fun isComplete(): Boolean = fee.isComplete()
}

fun ParkingFeeAnswer.applyTo(tags: Tags) {
    when (this) {
        is ParkingFee -> fee.applyTo(tags)
        is ParkingFeeAnswer.NoFeeButMaxStay -> {
            Fee.No.applyTo(tags)
            maxstay.applyTo(tags)
        }
    }
}
