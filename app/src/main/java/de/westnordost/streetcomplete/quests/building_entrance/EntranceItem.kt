package de.westnordost.streetcomplete.quests.building_entrance

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.image_select.Item

fun List<EntranceAnswer>.toItems() =
    map { it.asItem() }

fun EntranceAnswer.asItem(): Item<EntranceAnswer> = when (this) {
    DeadEnd ->  Item(this, R.drawable.ic_railway_crossing_chicane, /* ic_building_entrance_dead_end */ R.string.quest_building_entrance_dead_end)
    EntranceExistsAnswer.MAIN ->  Item(this, R.drawable.ic_railway_crossing_chicane, R.string.quest_building_entrance_main)
    EntranceExistsAnswer.SECONDARY -> Item(this, R.drawable.ic_railway_crossing_chicane, R.string.quest_building_entrance_secondary)
    EntranceExistsAnswer.SERVICE ->  Item(this, R.drawable.ic_railway_crossing_chicane, R.string.quest_building_entrance_service)
    EntranceExistsAnswer.GARAGE -> Item(this, R.drawable.ic_railway_crossing_chicane, R.string.quest_building_entrance_garage)
    EntranceExistsAnswer.EXIT -> Item(this, R.drawable.ic_railway_crossing_chicane, R.string.quest_building_entrance_exit)
    EntranceExistsAnswer.EMERGENCY_EXIT -> Item(this, R.drawable.ic_railway_crossing_chicane, R.string.quest_building_entrance_emergency_exit)
    EntranceExistsAnswer.SHOP -> Item(this, R.drawable.ic_railway_crossing_chicane, R.string.quest_building_entrance_shop)
    EntranceExistsAnswer.YES -> Item(this, R.drawable.ic_railway_crossing_chicane, R.string.quest_building_entrance_yes)
}
