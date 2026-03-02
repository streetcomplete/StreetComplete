package de.westnordost.streetcomplete.quests.max_speed

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.maxspeed.COUNTRY_SUBDIVISIONS_WITH_OWN_DEFAULT_MAX_SPEEDS
import de.westnordost.streetcomplete.osm.maxspeed.ROADS_THAT_THAT_MAY_BE_CONVERTED_TO_LIVING_STREET
import de.westnordost.streetcomplete.osm.maxspeed.Speed
import de.westnordost.streetcomplete.util.ktx.toYesNo

sealed interface MaxSpeedAnswer {
    data object IsLivingStreet : MaxSpeedAnswer
}
data class MaxSpeedSign(val speed: Speed) : MaxSpeedAnswer
data class AdvisorySpeedSign(val speed: Speed) : MaxSpeedAnswer
data class MaxSpeedZone(val speed: Speed) : MaxSpeedAnswer
data class DefaultMaxSpeed(val roadType: RoadType?) : MaxSpeedAnswer

/** apply this max speed answer to the given [tags] in the given region identified by its
 *  [countryOrSubdivisionCode] (e.g. "DE" or "US-AK") */
fun MaxSpeedAnswer.applyTo(tags: Tags, countryOrSubdivisionCode: String) {
    if (tags.containsKey("living_street")) {
        tags["living_street"] = (this is MaxSpeedAnswer.IsLivingStreet).toYesNo()
    }

    val cc = countryOrSubdivisionCode
    val useSubdivisionCode = COUNTRY_SUBDIVISIONS_WITH_OWN_DEFAULT_MAX_SPEEDS.any { it.matches(cc) }
    val maxspeedCountryCode = if (useSubdivisionCode) cc else cc.split("-").first()

    when (this) {
        is MaxSpeedSign -> {
            tags["maxspeed"] = speed.toOsmString()
            tags["maxspeed:type"] = "sign"
        }
        is MaxSpeedZone -> {
            tags["maxspeed"] = speed.toOsmString()
            tags["maxspeed:type"] = maxspeedCountryCode + ":zone" + speed.value.toString() // e.g. zone30
        }
        is AdvisorySpeedSign -> {
            tags["maxspeed:advisory"] = speed.toOsmString()
            tags["maxspeed:type:advisory"] = "sign"
        }
        is MaxSpeedAnswer.IsLivingStreet -> {
            // according to wiki, if it is a service road like a parking lot or a footway etc,
            // living_street=yes should be used instead
            if (tags["highway"] in ROADS_THAT_THAT_MAY_BE_CONVERTED_TO_LIVING_STREET) {
                tags["highway"] = "living_street"
            } else {
                tags["living_street"] = "yes"
            }
        }
        is DefaultMaxSpeed -> {
            val roadTypeString = roadType?.osmValue ?: tags["highway"]
            tags["maxspeed:type"] = maxspeedCountryCode + ":" + roadTypeString
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

fun MaxSpeedAnswer.getSpeedOrNull(): Speed? = when (this) {
    is AdvisorySpeedSign -> speed
    is MaxSpeedSign -> speed
    is MaxSpeedZone -> speed
    else -> null
}
