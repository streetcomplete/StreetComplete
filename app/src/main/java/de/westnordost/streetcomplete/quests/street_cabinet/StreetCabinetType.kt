package de.westnordost.streetcomplete.quests.street_cabinet

enum class StreetCabinetType(val osmKey: String, val osmValue: String) {
    POWER("utility", "power"),
    TELECOM("utility", "telecom"),
    TRAFFIC_CONTROL("street_cabinet", "traffic_control"),
    POSTAL_SERVICE("street_cabinet", "postal_service"),
    GAS("utility", "gas"),
    STREET_LIGHTING("utility", "street_lighting"),
    TRANSPORT_MANAGEMENT("street_cabinet", "transport_management"),
    TRAFFIC_MONITORING("street_cabinet", "traffic_monitoring"),
    WASTE("street_cabinet", "waste"),
    TELEVISION("utility", "television"),
    WATER("utility", "water"),
    SEWERAGE("utility", "sewerage");
}
