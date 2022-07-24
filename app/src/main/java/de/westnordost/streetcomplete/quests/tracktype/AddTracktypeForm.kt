package de.westnordost.streetcomplete.quests.tracktype

import de.westnordost.streetcomplete.osm.Tracktype
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.view.image_select.Item

class AddTracktypeForm : AImageListQuestForm<Tracktype, Tracktype>() {

    override val items: List<Item<Tracktype>> = Tracktype.items()

    override val itemsPerRow = 3

    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<Tracktype>) {
        applyAnswer(selectedItems.single())
    }
}
