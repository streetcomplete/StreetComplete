package de.westnordost.streetcomplete.quests.street_cabinet

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.street_cabinet.StreetCabinetType.*
import de.westnordost.streetcomplete.view.image_select.Item

fun StreetCabinetType.asItem() = Item(this, iconResId, titleResId)

private val StreetCabinetType.titleResId: Int get() = when (this) {
    POWER ->                R.string.quest_utility_power
    TELECOM ->              R.string.quest_utility_telecom
    POSTAL_SERVICE ->       R.string.quest_street_cabinet_postal_service
    TRAFFIC_CONTROL ->      R.string.quest_street_cabinet_traffic_control
    TRAFFIC_MONITORING ->   R.string.quest_street_cabinet_traffic_monitoring
    TRANSPORT_MANAGEMENT -> R.string.quest_street_cabinet_transport_management
    WASTE ->                R.string.quest_street_cabinet_waste
    TELEVISION ->           R.string.quest_street_cabinet_television
    GAS ->                  R.string.quest_utility_gas
    STREET_LIGHTING ->      R.string.quest_street_cabinet_street_lighting
    WATER ->                R.string.quest_utility_water
    SEWERAGE ->             R.string.quest_utility_sewerage
}

private val StreetCabinetType.iconResId: Int get() = when (this) {
    POWER ->                R.drawable.quest_street_cabinet_power
    TELECOM ->              R.drawable.quest_street_cabinet_telecom
    POSTAL_SERVICE ->       R.drawable.quest_street_cabinet_postal_service
    TRAFFIC_CONTROL ->      R.drawable.quest_street_cabinet_traffic_control
    TRAFFIC_MONITORING ->   R.drawable.quest_street_cabinet_traffic_monitoring
    TRANSPORT_MANAGEMENT -> R.drawable.quest_street_cabinet_transport_management
    WASTE ->                R.drawable.quest_street_cabinet_waste
    TELEVISION ->           R.drawable.quest_street_cabinet_television
    GAS ->                  R.drawable.quest_street_cabinet_gas
    STREET_LIGHTING ->      R.drawable.quest_street_cabinet_street_lighting
    WATER ->                R.drawable.quest_street_cabinet_water
    SEWERAGE ->             R.drawable.quest_street_cabinet_sewerage
}
