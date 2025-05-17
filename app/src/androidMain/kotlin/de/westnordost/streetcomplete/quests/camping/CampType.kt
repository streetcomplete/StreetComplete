package de.westnordost.streetcomplete.quests.camping

enum class CampType(val tents: Boolean, val caravans: Boolean) {
    TENTS_AND_CARAVANS(true, true),
    TENTS_ONLY(true, false),
    CARAVANS_ONLY(false, true),
    // Only the enum value is used here, the tent and caravan values are ignored.
    BACKCOUNTRY(false, false),
}
