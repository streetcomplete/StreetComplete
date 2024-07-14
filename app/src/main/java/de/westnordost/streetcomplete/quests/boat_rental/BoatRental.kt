package de.westnordost.streetcomplete.quests.boat_rental

// sorted by taginfo usages as per July 2024
enum class BoatRental(val osmValue: String) {
    CANOE("canoe_rental"),
    KAYAK("kayak_rental"),
    PEDALBOARD("pedalboard_rental"),
    MOTORBOAT("motorboat_rental"),
    PADDLEBOARD("standup_paddleboard_rental"),
    SAILBOAT("sailboat_rental"),
    JETSKI("jetski_rental"),
    HOUSEBOAT("houseboat_rental"),
    DINGHY("dinghy_rental"),
}
