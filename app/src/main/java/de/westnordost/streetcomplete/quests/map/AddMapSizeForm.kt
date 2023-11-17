package de.westnordost.streetcomplete.quests.map

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem

class AddMapSizeForm : AListQuestForm<String>() {
    override val items = listOf(
        TextItem("site", R.string.quest_mapSize_site),
        TextItem("city", R.string.quest_mapSize_city),
        TextItem("landscape", R.string.quest_mapSize_landscape),
        TextItem("region", R.string.quest_mapSize_region),
    )
}
