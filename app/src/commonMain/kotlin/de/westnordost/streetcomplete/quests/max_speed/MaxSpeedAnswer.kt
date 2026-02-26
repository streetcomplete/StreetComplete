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
data class MaxSpeedZone(val countryCode: String, val value: Speed) : MaxSpeedAnswer
data class DefaultMaxSpeed(val countryCode: String, val roadType: RoadType?) : MaxSpeedAnswer

// the reason why the road type needs to be specified is because there no way to tag just "there is
// no sign" for roads that are not assumed to have a default speed limit (motorways, living streets,
// ...).
sealed interface RoadType {
    fun toOsmString(): String

    // Tagging rural or urban is a straightforward alternative. The legislation in most countries
    // strictly separates urban and rural roads in regards to defining default speed limits, so this
    // is also actually useful information.
    //
    // For those few who do not make this distinction (but another), it's not detrimental to tag it
    // anyway like that. E.g. in many states of the USA, there is often no single default speed
    // limit for built-up areas, yet, almost universally, there is at least one for *rural* areas.
    // So even there, this distinction is helpful to determine the actual default speed limit.
    //
    // Note that the actual definition of what counts as a built-up area (in regards to speed
    // limits) differs per legislation, of course.
    enum class RuralOrUrban(private val osmValue: String) : RoadType {
        RURAL("rural"),
        URBAN("urban");

        override fun toOsmString() = osmValue
    }

    // The only reason why this special case exists for United Kingdom are legacy reasons.
    // In the UK, default speed limits have always been tagged with maxspeed:type =
    // GB:nsl_restricted  - when the street is lit (i.e. considered in built-up area in regards to
    //                      max speed)
    // GB:nsl_single      - not lit (i.e. rural roads)
    // GB:nsl_dual        - not lit (i.e. rural roads) but on a dual carriageway
    enum class UnitedKingdom(private val osmValue: String) : RoadType {
        RESTRICTED("nsl_restricted"),
        SINGLE("nsl_single"),
        DUAL("nsl_dual");

        override fun toOsmString() = osmValue
    }
}

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
            val roadTypeString = roadType?.toOsmString() ?: tags["highway"]
            tags["maxspeed:type"] = countryCode + ":" + roadTypeString
            // Special for United Kingdom: User implicitly answered whether road is lit or not
            if (roadType is RoadType.UnitedKingdom) {
                tags["lit"] = (roadType == RoadType.UnitedKingdom.RESTRICTED).toYesNo()
            }
        }
    }
}
