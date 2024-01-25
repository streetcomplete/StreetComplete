package de.westnordost.streetcomplete.quests.leaf_detail

import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddForestLeafTypeForm : AImageListQuestForm<ForestLeafType, ForestLeafType>() {

    override val items by lazy {
        val types = if (element is Node)
            listOf(ForestLeafType.NEEDLELEAVED, ForestLeafType.BROADLEAVED)
        else
            ForestLeafType.entries
        types.map { it.asItem() }
    }
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<ForestLeafType>) {
        applyAnswer(selectedItems.single())
    }
}
