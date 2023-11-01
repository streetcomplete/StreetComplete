package de.westnordost.streetcomplete.quests.shelter_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.IAnswerItem
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.PUBLIC_TRANSPORT
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.PICNIC_SHELTER
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.GAZEBO
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.LEAN_TO
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.BASIC_HUT
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.SUN_SHELTER
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.FIELD_SHELTER
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.ROCK_SHELTER
import de.westnordost.streetcomplete.quests.shelter_type.ShelterType.WEATHER_SHELTER
import de.westnordost.streetcomplete.view.image_select.Item

class AddShelterTypeForm : AImageListQuestForm<ShelterType, ShelterType>() {

    override val items = listOf(
        Item(PUBLIC_TRANSPORT, R.drawable.shelter_type_public_transport, R.string.quest_shelter_type_public_transport),
        Item(PICNIC_SHELTER, R.drawable.shelter_type_picnic_shelter, R.string.quest_shelter_type_picnic_shelter),
        Item(GAZEBO, R.drawable.shelter_type_gazebo, R.string.quest_shelter_type_gazebo),
        Item(LEAN_TO, R.drawable.shelter_type_lean_to, R.string.quest_shelter_type_lean_to),
        Item(BASIC_HUT, R.drawable.shelter_type_basic_hut, R.string.quest_shelter_type_basic_hut),
        Item(SUN_SHELTER, R.drawable.shelter_type_sun_shelter, R.string.quest_shelter_type_sun_shelter),
        Item(FIELD_SHELTER, R.drawable.shelter_type_field_shelter, R.string.quest_shelter_type_field_shelter),
        Item(ROCK_SHELTER, R.drawable.shelter_type_rock_shelter, R.string.quest_shelter_type_rock_shelter)
    )

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_shelter_type_is_weather_shelter) { applyAnswer(WEATHER_SHELTER) }
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<ShelterType>) {
        applyAnswer(selectedItems.single())
    }
}
