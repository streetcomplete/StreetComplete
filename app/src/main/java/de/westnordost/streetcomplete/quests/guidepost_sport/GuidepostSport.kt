package de.westnordost.streetcomplete.quests.guidepost_sport

enum class GuidepostSport(val key: String) {
    HIKING("hiking"),
    BICYCLE("bicycle"),
    MTB("mtb"),
    CLIMBING("climbing"),
    HORSE("horse"),
    NORDIC_WALKING("nordic_walking"),
    SKI("ski"),
    INLINE_SKATING("inline_skating"),
    RUNNING("running"),
    WINTER_HIKING("winter_hiking");

    companion object {
        val selectableValues = listOf(
            HIKING, BICYCLE, MTB, CLIMBING, HORSE, NORDIC_WALKING, SKI, INLINE_SKATING, RUNNING, WINTER_HIKING
        )
    }
}
