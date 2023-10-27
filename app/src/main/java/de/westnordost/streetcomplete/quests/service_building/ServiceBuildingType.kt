package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.service_building.ServiceBuildingType.*
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

enum class ServiceBuildingType(val tags: List<Pair<String, String>>) {
    POWER(listOf("utility" to "power")),
    TELECOM(listOf("utility" to "telecom")),
    WATER(listOf("utility" to "water")),
    GAS(listOf("utility" to "gas")),
    SEWERAGE(listOf("utility" to "sewerage", "substance" to "sewage")), // can be pumping stations or treatment plants
    HEATING(listOf("utility" to "heating")),
    VENTILATION_SHAFT(listOf("man_made" to "ventilation")), // building tag removed in AddServiceBuildingType.applyAnswerTo
    MONITORING_STATION(listOf("man_made" to "monitoring_station")),
    // POWER
    MINOR_SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "minor_distribution")),
    SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "distribution")),
    INDUSTRIAL_SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "industrial")),
    TRACTION_SUBSTATION(listOf("utility" to "power", "power" to "substation", "substation" to "traction")),
    SWITCHGEAR(listOf("utility" to "power", "power" to "switchgear")),
    PLANT(listOf("utility" to "power", "power" to "plant")),
    //GAS
    GAS_PRESSURE_REGULATION(listOf("utility" to "gas", "pipeline" to "substation", "substation" to "distribution", "substance" to "gas")),
    GAS_PUMPING_STATION(listOf("utility" to "gas", "man_made" to "pumping_station", "substance" to "gas")),
    // WATER
    WATER_WELL(listOf("utility" to "water", "man_made" to "water_well", "substance" to "water")),
    COVERED_RESERVOIR(listOf("utility" to "water", "man_made" to "reservoir_covered", "substance" to "water")),
    WATER_PUMPING_STATION(listOf("utility" to "water", "man_made" to "pumping_station", "substance" to "water")),
    // OIL
    OIL_PUMPING_STATION(listOf("utility" to "oil", "man_made" to "pumping_station", "substance" to "oil")),
    // RAILWAY
    RAILWAY_VENTILATION_SHAFT(listOf("service" to "ventilation", "railway" to "ventilation_shaft")),
    RAILWAY_SIGNAL_BOX(listOf("building" to "industrial", "railway" to "signal_box")),
    RAILWAY_ENGINE_SHED(listOf("building" to "industrial", "railway" to "engine_shed")),
    RAILWAY_WASH(listOf("building" to "industrial", "railway" to "wash")),
    // TELECOM
    INTERNET_EXCHANGE(listOf("utility" to "communication", "telecom" to "internet_exchange")),
    TELECOM_EXCHANGE(listOf("utility" to "communication", "telecom" to "exchange")),
}

enum class ServiceBuildingTypeCategory(val type: ServiceBuildingType?, val subTypes: List<ServiceBuildingType>) {
    POWER(ServiceBuildingType.POWER, listOf(MINOR_SUBSTATION, SUBSTATION, INDUSTRIAL_SUBSTATION, TRACTION_SUBSTATION, SWITCHGEAR, PLANT)),
    WATER(ServiceBuildingType.WATER, listOf(WATER_WELL, COVERED_RESERVOIR, WATER_PUMPING_STATION)),
    GAS(ServiceBuildingType.GAS, listOf(GAS_PUMPING_STATION, GAS_PRESSURE_REGULATION)),
    TELECOM(ServiceBuildingType.TELECOM, listOf(TELECOM_EXCHANGE, INTERNET_EXCHANGE)),
    RAILWAY(null, listOf(RAILWAY_VENTILATION_SHAFT, RAILWAY_SIGNAL_BOX, RAILWAY_ENGINE_SHED, RAILWAY_WASH)),
    OTHER_SERVICE(null, listOf(OIL_PUMPING_STATION, SEWERAGE, HEATING, VENTILATION_SHAFT, MONITORING_STATION)),
}

fun Collection<ServiceBuildingType>.toItems() = map { it.asItem() }
fun Array<ServiceBuildingTypeCategory>.toItems() = map { it.asItem() }

fun ServiceBuildingType.asItem(): GroupableDisplayItem<ServiceBuildingType> {
    return Item(this, iconResId, titleResId, descriptionResId)
}

fun ServiceBuildingTypeCategory.asItem(): GroupableDisplayItem<ServiceBuildingType> {
    return Item(type, iconResId, titleResId, null, subTypes.toItems())
}

private val ServiceBuildingType.titleResId: Int get() = when (this) {
    POWER -> R.string.quest_utility_power
    MINOR_SUBSTATION -> R.string.quest_service_building_type_minor_substation
    SUBSTATION -> R.string.quest_service_building_type_substation
    INDUSTRIAL_SUBSTATION -> R.string.quest_service_building_type_industrial_substation
    TRACTION_SUBSTATION -> R.string.quest_service_building_type_traction_substation
    SWITCHGEAR -> R.string.quest_service_building_type_switchgear
    PLANT -> R.string.quest_service_building_type_plant
    WATER -> R.string.quest_utility_water
    WATER_WELL -> R.string.quest_service_building_type_well
    COVERED_RESERVOIR -> R.string.quest_service_building_type_reservoir
    WATER_PUMPING_STATION -> R.string.quest_service_building_type_pump
    SEWERAGE -> R.string.quest_utility_sewerage
    OIL_PUMPING_STATION -> R.string.quest_service_building_oil_pumping_station
    GAS -> R.string.quest_utility_gas
    GAS_PRESSURE_REGULATION -> R.string.quest_service_building_type_pressure
    GAS_PUMPING_STATION -> R.string.quest_service_building_gas_pumping_station
    TELECOM -> R.string.quest_utility_telecom
    TELECOM_EXCHANGE -> R.string.quest_service_building_telecom_exchange
    INTERNET_EXCHANGE -> R.string.quest_service_building_internet_exchange
    RAILWAY_VENTILATION_SHAFT -> R.string.quest_service_building_railway_ventilation_shaft
    RAILWAY_SIGNAL_BOX -> R.string.quest_service_building_railway_signal_box
    RAILWAY_ENGINE_SHED -> R.string.quest_service_building_railway_engine_shed
    RAILWAY_WASH -> R.string.quest_service_building_railway_wash
    VENTILATION_SHAFT -> R.string.quest_service_building_ventilation
    HEATING -> R.string.quest_service_building_heating
    MONITORING_STATION -> R.string.quest_service_building_monitoring_station
}

private val ServiceBuildingType.descriptionResId: Int? get() = when (this) {
    MINOR_SUBSTATION -> R.string.quest_service_building_type_minor_substation_description
    SUBSTATION -> R.string.quest_service_building_type_substation_description
    INDUSTRIAL_SUBSTATION -> R.string.quest_service_building_type_industrial_substation_description
    TRACTION_SUBSTATION -> R.string.quest_service_building_type_traction_substation_description
    SWITCHGEAR -> R.string.quest_service_building_type_switchgear_description
    WATER_WELL -> R.string.quest_service_building_type_well_description
    COVERED_RESERVOIR -> R.string.quest_service_building_type_reservoir_description
    WATER_PUMPING_STATION -> R.string.quest_service_building_type_pump_description
    SEWERAGE -> R.string.quest_service_building_sewerage_description
    OIL_PUMPING_STATION -> R.string.quest_service_building_oil_pumping_station_description
    GAS_PRESSURE_REGULATION -> R.string.quest_service_building_type_pressure_description
    GAS_PUMPING_STATION -> R.string.quest_service_building_gas_pumping_station_description
    TELECOM_EXCHANGE -> R.string.quest_service_building_telecom_exchange_description
    INTERNET_EXCHANGE -> R.string.quest_service_building_internet_exchange_description
    RAILWAY_VENTILATION_SHAFT -> R.string.quest_service_building_railway_ventilation_shaft_description
    RAILWAY_SIGNAL_BOX -> R.string.quest_service_building_railway_signal_box_description
    RAILWAY_ENGINE_SHED -> R.string.quest_service_building_railway_engine_shed_description
    RAILWAY_WASH -> R.string.quest_service_building_railway_wash_description
    VENTILATION_SHAFT -> R.string.quest_service_building_ventilation_description
    HEATING -> R.string.quest_service_building_heating_description
    MONITORING_STATION -> R.string.quest_service_building_monitoring_station_description
    else -> null
}

private val ServiceBuildingType.iconResId: Int get() = when (this) {
    POWER -> R.drawable.ic_quest_service_building_power
    WATER ->    R.drawable.ic_quest_service_building_water
    TELECOM ->    R.drawable.ic_quest_service_building_telecom
    GAS ->    R.drawable.ic_quest_building_service_gas
    SEWERAGE ->    R.drawable.ic_quest_service_building_sewerage
    MINOR_SUBSTATION ->    R.drawable.ic_quest_service_building_minor_substation
    SUBSTATION ->    R.drawable.ic_quest_service_building_substation
    INDUSTRIAL_SUBSTATION ->    R.drawable.ic_quest_service_building_industrial_substation
    TRACTION_SUBSTATION ->    R.drawable.ic_quest_service_building_traction_substation
    SWITCHGEAR ->    R.drawable.ic_quest_service_building_switchgear
    PLANT ->    R.drawable.ic_quest_service_building_power_plant
    GAS_PRESSURE_REGULATION ->    R.drawable.ic_quest_building_service_gas_pressure
    GAS_PUMPING_STATION ->    R.drawable.ic_quest_building_service_gas_pump
    WATER_WELL ->    R.drawable.ic_quest_service_building_water_well
    COVERED_RESERVOIR ->    R.drawable.ic_quest_service_reservoir_covered
    WATER_PUMPING_STATION ->    R.drawable.ic_quest_service_building_water_pump
    OIL_PUMPING_STATION ->    R.drawable.ic_quest_service_building_oil_pump
    RAILWAY_VENTILATION_SHAFT ->    R.drawable.ic_quest_service_building_railway_ventilation
    RAILWAY_SIGNAL_BOX ->    R.drawable.ic_quest_service_building_railway_signal_box
    RAILWAY_ENGINE_SHED ->    R.drawable.ic_quest_service_building_railway_engine_shed
    RAILWAY_WASH ->    R.drawable.ic_quest_service_building_railway_wash
    HEATING ->    R.drawable.ic_quest_service_building_heating
    VENTILATION_SHAFT ->    R.drawable.ic_quest_service_building_ventilation
    TELECOM_EXCHANGE ->    R.drawable.ic_quest_service_building_telecom_exchange
    INTERNET_EXCHANGE ->    R.drawable.ic_quest_service_building_internet_exchange
    MONITORING_STATION ->    R.drawable.ic_quest_service_building_monitoring
}

private val ServiceBuildingTypeCategory.titleResId: Int get() = when (this) {
    ServiceBuildingTypeCategory.POWER -> R.string.quest_utility_power
    ServiceBuildingTypeCategory.WATER -> R.string.quest_utility_water
    ServiceBuildingTypeCategory.GAS -> R.string.quest_utility_gas
    ServiceBuildingTypeCategory.TELECOM -> R.string.quest_utility_telecom
    ServiceBuildingTypeCategory.RAILWAY -> R.string.quest_service_building_railway
    ServiceBuildingTypeCategory.OTHER_SERVICE -> R.string.quest_service_building_other
}

private val ServiceBuildingTypeCategory.iconResId: Int get() = when (this) {
    ServiceBuildingTypeCategory.POWER -> R.drawable.ic_quest_service_building_power
    ServiceBuildingTypeCategory.WATER -> R.drawable.ic_quest_service_building_water
    ServiceBuildingTypeCategory.GAS -> R.drawable.ic_quest_building_service_gas
    ServiceBuildingTypeCategory.TELECOM -> R.drawable.ic_quest_service_building_telecom
    ServiceBuildingTypeCategory.RAILWAY -> R.drawable.ic_quest_service_building_railway
    ServiceBuildingTypeCategory.OTHER_SERVICE -> R.drawable.ic_quest_service_building_other
}
