package de.westnordost.streetcomplete.quests.hairdresser

import de.westnordost.streetcomplete.quests.hairdresser.HairdresserCustomers.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_hairdresser_female_only
import de.westnordost.streetcomplete.resources.quest_hairdresser_male_and_female
import de.westnordost.streetcomplete.resources.quest_hairdresser_male_only
import de.westnordost.streetcomplete.resources.quest_hairdresser_not_signed
import org.jetbrains.compose.resources.StringResource

enum class HairdresserCustomers(val isMale: Boolean, val isFemale: Boolean) {
    MALE_AND_FEMALE(true, true),
    ONLY_FEMALE(false, true),
    ONLY_MALE(true, false),
    NOT_SIGNED(false, false),
}

val HairdresserCustomers.text: StringResource get() = when (this) {
    MALE_AND_FEMALE -> Res.string.quest_hairdresser_male_and_female
    ONLY_FEMALE -> Res.string.quest_hairdresser_female_only
    ONLY_MALE -> Res.string.quest_hairdresser_male_only
    NOT_SIGNED -> Res.string.quest_hairdresser_not_signed
}
