package de.westnordost.streetcomplete.quests.fuel_type

import de.westnordost.streetcomplete.quests.fuel_type.FuelTypes.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val FuelTypes.title: StringResource get() = when (this) {
    CNG   ->  Res.string.quest_fuel_types_cng
    DIESEL   ->  Res.string.quest_fuel_types_diesel
    E10   ->  Res.string.quest_fuel_types_e10
    E85   ->  Res.string.quest_fuel_types_e85
    LPG   ->  Res.string.quest_fuel_types_lpg
    OCTANE_85   ->  Res.string.quest_fuel_types_octane_85
    OCTANE_87   ->  Res.string.quest_fuel_types_octane_87
    OCTANE_89   ->  Res.string.quest_fuel_types_octane_89
    OCTANE_91   ->  Res.string.quest_fuel_types_octane_91
    OCTANE_92   ->  Res.string.quest_fuel_types_octane_92
    OCTANE_95   ->  Res.string.quest_fuel_types_octane_95
    OCTANE_98   ->  Res.string.quest_fuel_types_octane_98
}

val FuelTypes.icon: DrawableResource get() = when (this) {
    CNG   ->  Res.drawable.recycling_engine_oil
    DIESEL   ->  Res.drawable.recycling_engine_oil
    E10   ->  Res.drawable.recycling_engine_oil
    E85   ->  Res.drawable.recycling_engine_oil
    LPG   ->  Res.drawable.recycling_engine_oil
    OCTANE_85   ->  Res.drawable.recycling_engine_oil
    OCTANE_87   ->  Res.drawable.recycling_engine_oil
    OCTANE_89   ->  Res.drawable.recycling_engine_oil
    OCTANE_91   ->  Res.drawable.recycling_engine_oil
    OCTANE_92   ->  Res.drawable.recycling_engine_oil
    OCTANE_95   ->  Res.drawable.recycling_engine_oil
    OCTANE_98   ->  Res.drawable.recycling_engine_oil
}
