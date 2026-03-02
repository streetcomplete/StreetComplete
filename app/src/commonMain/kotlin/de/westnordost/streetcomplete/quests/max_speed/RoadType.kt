package de.westnordost.streetcomplete.quests.max_speed

// the reason why the road type needs to be specified is because there no way to tag just "there is
// no sign" for roads that are not assumed to have a default speed limit (motorways, living streets,
// ...).
enum class RoadType(val osmValue: String) {
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
    RURAL("rural"),
    URBAN("urban"),

    // The only reason why this special case exists for United Kingdom are legacy reasons.
    // In the UK, default speed limits have always been tagged with maxspeed:type = ...
    // on lit streets (i.e. considered in built-up area in regards to max speed)
    RESTRICTED("nsl_restricted"),
    // when not lit (i.e. rural roads)
    SINGLE("nsl_single"),
    // when not lit (i.e. rural roads) but on a dual carriageway
    DUAL("nsl_dual");

    companion object {
        fun getEntriesByCountryCode(countryCode: String) : List<RoadType> = when (countryCode) {
            "GB" -> listOf(RESTRICTED, SINGLE, DUAL)
            else -> listOf(URBAN, RURAL)
        }
    }
}
