package de.westnordost.streetcomplete.quests.camping

enum class CampType(val tents: Boolean, val caravans: Boolean) {
    TENTS_AND_CARAVANS(true, true),
    TENTS_ONLY(true, false),
    CARAVANS_ONLY(false, true),
    BACKCOUNTRY(false, false),
}
