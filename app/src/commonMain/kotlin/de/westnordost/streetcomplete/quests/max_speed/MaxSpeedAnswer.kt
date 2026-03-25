package de.westnordost.streetcomplete.quests.max_speed

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.maxspeed.COUNTRY_SUBDIVISIONS_WITH_OWN_DEFAULT_MAX_SPEEDS
import de.westnordost.streetcomplete.osm.maxspeed.ROADS_THAT_THAT_MAY_BE_CONVERTED_TO_LIVING_STREET
import de.westnordost.streetcomplete.osm.maxspeed.Speed
import de.westnordost.streetcomplete.quests.max_speed.MaxSpeedSign.Type.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo
import org.jetbrains.compose.resources.StringResource

sealed interface MaxSpeedAnswer {
    fun isComplete(): Boolean

    data object IsLivingStreet : MaxSpeedAnswer {
        override fun isComplete()  = true
    }
    data object NoSign : MaxSpeedAnswer {
        override fun isComplete()  = true
    }
    data class NoSignWithRoadType(val roadType: RoadType?) : MaxSpeedAnswer {
        override fun isComplete() = roadType != null
    }
}
// grouped the three different maxspeed sign types like this instead of each their own data classes
// because other than design of the sign, they all need the same input: integer number input plus
// maybe a unit selector
data class MaxSpeedSign(val speed: Speed, val type: Type) : MaxSpeedAnswer {
    override fun isComplete() = speed.isComplete()

    enum class Type { NORMAL, ADVISORY, ZONE }
}

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
            when (type) {
                NORMAL -> {
                    tags["maxspeed"] = speed.toOsmString()
                    tags["maxspeed:type"] = "sign"
                }
                ADVISORY -> {
                    tags["maxspeed:advisory"] = speed.toOsmString()
                    tags["maxspeed:type:advisory"] = "sign"
                }
                ZONE -> {
                    tags["maxspeed"] = speed.toOsmString()
                    //                                                      e.g. zone30
                    tags["maxspeed:type"] = maxspeedCountryCode + ":zone" + speed.value!!.toString()
                }
            }
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
        is MaxSpeedAnswer.NoSignWithRoadType -> {
            tags["maxspeed:type"] = maxspeedCountryCode + ":" + roadType!!.osmValue
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
        is MaxSpeedAnswer.NoSign -> {
            tags["maxspeed:type"] = maxspeedCountryCode + ":" + tags["highway"]
        }
    }
}

val MaxSpeedAnswer.text: StringResource get() = when (this) {
    is MaxSpeedSign ->
        when (type) {
            NORMAL ->   Res.string.quest_maxspeed_answer_sign
            ADVISORY -> Res.string.quest_maxspeed_answer_advisory_speed_limit2
            ZONE ->     Res.string.quest_maxspeed_answer_zone2
        }
    is MaxSpeedAnswer.NoSignWithRoadType, MaxSpeedAnswer.NoSign ->
        Res.string.quest_maxspeed_answer_noSign2
    MaxSpeedAnswer.IsLivingStreet ->
        Res.string.quest_maxspeed_answer_living_street
}
