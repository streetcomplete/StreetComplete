package de.westnordost.streetcomplete.quests.camping

enum class CampType(val tents: Boolean, val caravans: Boolean) {
    TENTS_AND_CARAVANS(true, true),
    CARAVANS_ONLY(false, true),
    TENTS_ONLY(true, false),
    BACKCOUNTRY(false, false),
}
