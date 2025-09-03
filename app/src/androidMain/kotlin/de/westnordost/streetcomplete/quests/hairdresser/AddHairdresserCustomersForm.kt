package de.westnordost.streetcomplete.quests.hairdresser

import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.hairdresser.HairdresserCustomers.MALE_AND_FEMALE
import de.westnordost.streetcomplete.quests.hairdresser.HairdresserCustomers.NOT_SIGNED
import de.westnordost.streetcomplete.quests.hairdresser.HairdresserCustomers.ONLY_FEMALE
import de.westnordost.streetcomplete.quests.hairdresser.HairdresserCustomers.ONLY_MALE
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_hairdresser_female_only
import de.westnordost.streetcomplete.resources.quest_hairdresser_male_and_female
import de.westnordost.streetcomplete.resources.quest_hairdresser_male_only
import de.westnordost.streetcomplete.resources.quest_hairdresser_not_signed
import de.westnordost.streetcomplete.ui.common.TextItem

class AddHairdresserCustomersForm : AListQuestForm<HairdresserCustomers>() {
    override val items = listOf(
        TextItem(MALE_AND_FEMALE, Res.string.quest_hairdresser_male_and_female),
        TextItem(ONLY_FEMALE, Res.string.quest_hairdresser_female_only),
        TextItem(ONLY_MALE, Res.string.quest_hairdresser_male_only),
        TextItem(NOT_SIGNED, Res.string.quest_hairdresser_not_signed),
    )
}
