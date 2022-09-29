package de.westnordost.streetcomplete.quests.shelter_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.PUBLIC_TRANSPORT
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.PICNIC_SHELTER
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.GAZEBO
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.WEATHER_SHELTER
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.LEAN_TO
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.PAVILION
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.BASIC_HUT
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.SUN_SHELTER
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.FIELD_SHELTER
import de.westnordost.streetcomplete.view.image_select.Item

fun ShelterType.asItem() = Item(this, iconResId, titleResId)

private val ShelterType.titleResId: Int get() = when (this) {
    PUBLIC_TRANSPORT -> R.string.quest_shelterType_public_transport
    PICNIC_SHELTER ->   R.string.quest_shelterType_picnic_shelter
    GAZEBO ->           R.string.quest_shelterType_gazebo
    WEATHER_SHELTER ->  R.string.quest_shelterType_weather_shelter
    LEAN_TO ->          R.string.quest_shelterType_lean_to
    PAVILION ->         R.string.quest_shelterType_pavilion
    BASIC_HUT ->        R.string.quest_shelterType_basic_hut
    SUN_SHELTER ->      R.string.quest_shelterType_sun_shelter
    FIELD_SHELTER ->    R.string.quest_shelterType_field_shelter
}

private val ShelterType.iconResId: Int get() = when (this) {
    PUBLIC_TRANSPORT -> R.drawable.shelter_type_public_transport
    PICNIC_SHELTER ->   R.drawable.shelter_type_picnic_shelter
    GAZEBO ->           R.drawable.shelter_type_gazebo
    WEATHER_SHELTER ->  R.drawable.shelter_type_weather_shelter
    LEAN_TO ->          R.drawable.shelter_type_lean_to
    PAVILION ->         R.drawable.shelter_type_pavilion
    BASIC_HUT ->        R.drawable.shelter_type_basic_hut
    SUN_SHELTER ->      R.drawable.shelter_type_sun_shelter
    FIELD_SHELTER ->    R.drawable.shelter_type_field_shelter
}
