package de.westnordost.streetcomplete.quests.shelter_type

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddShelterTypeForm : AImageListQuestForm<ShelterType, ShelterType>() {

    override val items = ShelterType.values().map { it.asItem() }
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<ShelterType>) {
        applyAnswer(selectedItems.single())
    }
}
