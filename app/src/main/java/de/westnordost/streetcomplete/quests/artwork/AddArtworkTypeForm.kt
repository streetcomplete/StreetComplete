package de.westnordost.streetcomplete.quests.artwork

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem

class AddArtworkTypeForm : AListQuestForm<String>() {
    override val items = listOf(
        TextItem("sculpture", R.string.quest_artwork_sculpture),
        TextItem("statue", R.string.quest_artwork_statue),
        TextItem("bust", R.string.quest_artwork_bust),
        TextItem("architecture", R.string.quest_artwork_architecture),
        TextItem("relief", R.string.quest_artwork_relief),
        TextItem("mural", R.string.quest_artwork_mural),
        TextItem("fountain", R.string.quest_artwork_fountain),
        TextItem("installation", R.string.quest_artwork_installation),
        TextItem("stone", R.string.quest_artwork_stone),
        TextItem("mosaic", R.string.quest_artwork_mosaic),
        TextItem("graffiti", R.string.quest_artwork_graffiti),
        TextItem("painting", R.string.quest_artwork_painting),
        TextItem("land_art", R.string.quest_artwork_land_art),
    )

}
