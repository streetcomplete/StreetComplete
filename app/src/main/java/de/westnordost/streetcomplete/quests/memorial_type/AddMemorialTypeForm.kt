package de.westnordost.streetcomplete.quests.memorial_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.BUST
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.OBELISK
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.PLAQUE
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.SCULPTURE
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.STATUE
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.STONE
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.STONE_STELE
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.WAR_MEMORIAL
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.WOODEN_STELE
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

class AddMemorialTypeForm : AImageListQuestForm<MemorialType, MemorialType>() {

    override val items: List<DisplayItem<MemorialType>> = listOf(
        Item(STATUE, R.drawable.memorial_type_statue, R.string.quest_memorialType_statue),
        Item(BUST, R.drawable.memorial_type_bust, R.string.quest_memorialType_bust),
        Item(PLAQUE, R.drawable.memorial_type_plaque, R.string.quest_memorialType_plaque),
        Item(WAR_MEMORIAL, R.drawable.memorial_type_war_memorial, R.string.quest_memorialType_war_memorial),
        Item(STONE, R.drawable.memorial_type_stone, R.string.quest_memorialType_stone),
        Item(OBELISK, R.drawable.memorial_type_obelisk, R.string.quest_memorialType_obelisk),
        Item(WOODEN_STELE, R.drawable.memorial_type_stele_wooden, R.string.quest_memorialType_stele_wooden),
        Item(STONE_STELE, R.drawable.memorial_type_stele_stone, R.string.quest_memorialType_stele_stone),
        Item(SCULPTURE, R.drawable.memorial_type_sculpture, R.string.quest_memorialType_sculpture),
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<MemorialType>) {
        applyAnswer(selectedItems.single())
    }
}
