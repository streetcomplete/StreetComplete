package de.westnordost.streetcomplete.quests.pitch_lit

enum class PitchLit(val osmValue: String) {
    NIGHT_AND_DAY("24/7"),
    AUTOMATIC("automatic"),
    YES("yes"),
    NO("no"),
}

fun Boolean.toPitchLit(): PitchLit = if (this) PitchLit.YES else PitchLit.NO
