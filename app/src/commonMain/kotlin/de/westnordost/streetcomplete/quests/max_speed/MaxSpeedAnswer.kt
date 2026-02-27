package de.westnordost.streetcomplete.quests.max_speed

import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.maxspeed.Speed
import de.westnordost.streetcomplete.util.ktx.toYesNo

sealed interface MaxSpeedAnswer {
    data object IsLivingStreet : MaxSpeedAnswer
}
data class MaxSpeedSign(val value: Speed) : MaxSpeedAnswer
data class AdvisorySpeedSign(val value: Speed) : MaxSpeedAnswer
data class MaxSpeedZone(val value: Speed) : MaxSpeedAnswer
data class DefaultMaxSpeed(val roadType: RoadType?) : MaxSpeedAnswer

fun MaxSpeedAnswer.applyTo(tags: Tags) {
    if (tags.containsKey("living_street")) {
        tags["living_street"] = (this is MaxSpeedAnswer.IsLivingStreet).toYesNo()
    }

    when (this) {
        is MaxSpeedSign -> {
            tags["maxspeed"] = value.toOsmString()
            tags["maxspeed:type"] = "sign"
        }
        is MaxSpeedZone -> {
            tags["maxspeed"] = value.toOsmString()
            tags["maxspeed:type"] = countryCode + ":zone" + value.value.toString() // e.g. zone30
        }
        is AdvisorySpeedSign -> {
            tags["maxspeed:advisory"] = value.toOsmString()
            tags["maxspeed:type:advisory"] = "sign"
        }
        is MaxSpeedAnswer.IsLivingStreet -> {
            // according to wiki, if it is a service road like a parking lot or a footway etc,
            // living_street=yes should be used instead
            if (tags["highway"] in ALL_PATHS || tags["highway"] == "service") {
                tags["living_street"] = "yes"
            } else {
                tags["highway"] = "living_street"
            }
        }
        is DefaultMaxSpeed -> {
            val roadTypeString = roadType?.osmValue ?: tags["highway"]
            tags["maxspeed:type"] = countryCode + ":" + roadTypeString
            // Special for United Kingdom: User implicitly answered whether road is lit or not
            when (roadType) {
                RoadType.RESTRICTED -> {
                    tags["lit"] = "yes"
                }
                RoadType.SINGLE -> {
                    tags["lit"] = "no"
                    if (tags.containsKey("dual_carriageway")) tags["dual_carriageway"] = "no"
                }
                RoadType.DUAL -> {
                    tags["lit"] = "no"
                    tags["dual_carriageway"] = "yes"
                }
                else -> { /* nothing */ }
            }
        }
    }
}
