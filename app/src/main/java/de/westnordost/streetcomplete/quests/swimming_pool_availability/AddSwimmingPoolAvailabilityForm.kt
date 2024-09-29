package de.westnordost.streetcomplete.quests.swimming_pool_availability

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.swimming_pool_availability.SwimmingPoolAvailability.INDOOR_AND_OUTDOOR
import de.westnordost.streetcomplete.quests.swimming_pool_availability.SwimmingPoolAvailability.NO
import de.westnordost.streetcomplete.quests.swimming_pool_availability.SwimmingPoolAvailability.ONLY_INDOOR
import de.westnordost.streetcomplete.quests.swimming_pool_availability.SwimmingPoolAvailability.ONLY_OUTDOOR

class AddSwimmingPoolAvailabilityForm : AListQuestForm<SwimmingPoolAvailability>() {
    override val items = listOf(
        TextItem(INDOOR_AND_OUTDOOR, R.string.quest_swimming_pool_indoor_and_outdoor),
        TextItem(ONLY_INDOOR, R.string.quest_swimming_pool_indoor_only),
        TextItem(ONLY_OUTDOOR, R.string.quest_swimming_pool_outdoor_only),
        TextItem(NO, R.string.quest_swimming_pool_no),
    )
}
