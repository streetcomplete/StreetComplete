package de.westnordost.streetcomplete.quests.camera_type

import de.westnordost.streetcomplete.quests.camera_type.CameraType.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val CameraType.title: StringResource get() = when (this) {
    DOME ->    Res.string.quest_camera_type_dome
    FIXED ->   Res.string.quest_camera_type_fixed
    PANNING -> Res.string.quest_camera_type_panning
}

val CameraType.icon: DrawableResource get() = when (this) {
    DOME ->    Res.drawable.camera_type_dome
    FIXED ->   Res.drawable.camera_type_fixed
    PANNING -> Res.drawable.camera_type_panning
}
