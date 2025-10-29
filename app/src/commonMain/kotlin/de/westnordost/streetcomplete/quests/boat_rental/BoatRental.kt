package de.westnordost.streetcomplete.quests.boat_rental

enum class BoatRental(val osmValue: String) {
    // sort order: first the small things
    CANOE("canoe_rental"),
    KAYAK("kayak_rental"),
    PEDALBOAT("pedalboat_rental"),
    SUP("standup_paddleboard_rental"),
    ROWBOAT("rowboat_rental"),
    SAILBOAT("sailboat_rental"),
    RAFT("raft_rental"),
    SURFBOARD("surfboard_rental"),
    SAILBOARD("sailboard_rental"),
    // then the big things
    MOTORBOAT("motorboat_rental"),
    JETSKI("jetski_rental"),
    HOUSEBOAT("houseboat_rental"),
    YACHT("yacht_rental"),
}
