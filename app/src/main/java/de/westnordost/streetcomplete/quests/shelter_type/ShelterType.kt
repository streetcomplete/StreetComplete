package de.westnordost.streetcomplete.quests.shelter_type

import de.westnordost.streetcomplete.osm.Tags

enum class ShelterType(val osmValue: String) {
    PUBLIC_TRANSPORT("public_transport"),
    PICNIC_SHELTER("picnic_shelter"),
    GAZEBO("gazebo"),
    WEATHER_SHELTER("weather_shelter"),
    LEAN_TO("lean_to"),
    PAVILION("pavilion"),
    BASIC_HUT("basic_hut"),
    SUN_SHELTER("sun_shelter"),
    FIELD_SHELTER("field_shelter"),
}

fun ShelterType.applyTo(tags: Tags) {
    tags["shelter_type"] = this.osmValue
}
