package de.westnordost.streetcomplete.quests.shelter_type

enum class ShelterType(val osmValue: String) {
    PUBLIC_TRANSPORT("public_transport"),
    PICNIC_SHELTER("picnic_shelter"),
    GAZEBO("gazebo"),
    LEAN_TO("lean_to"),
    BASIC_HUT("basic_hut"),
    SUN_SHELTER("sun_shelter"),
    FIELD_SHELTER("field_shelter"),
    ROCK_SHELTER("rock_shelter"),
    WEATHER_SHELTER("weather_shelter")
}
