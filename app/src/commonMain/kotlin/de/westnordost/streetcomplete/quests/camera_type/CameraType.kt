package de.westnordost.streetcomplete.quests.camera_type

import kotlinx.serialization.Serializable

@Serializable
enum class CameraType(val osmValue: String) {
    DOME("dome"),
    FIXED("fixed"),
    PANNING("panning"),
}
