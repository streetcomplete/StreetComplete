package de.westnordost.streetcomplete.quests.shelter_type

import kotlinx.serialization.Serializable

@Serializable
enum class ShelterType(val osmValue: String, val title: String) {
    PUBLIC_TRANSPORT("public_transport", "Public Transport Shelter"),
    PICNIC_SHELTER("picnic_shelter", "Picnic Shelter"),
    GAZEBO("gazebo", "Gazebo"),
    WEATHER_SHELTER("weather_shelter", "Weather Shelter"),
    LEAN_TO("lean_to", "Lean-to Shelter"),
    SUN_SHELTER("sun_shelter", "Sun Shelter"),
    PAVILION("pavilion", "Pavilion"),
    BASIC_HUT("basic_hut", "Basic Hut"),
    FIELD_SHELTER("field_shelter", "Field Shelter"),
}
