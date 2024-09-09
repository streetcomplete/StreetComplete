package de.westnordost.streetcomplete.quests.caravan_site_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem

class AddCaravanSiteTypeForm : AListQuestForm<String>() {
    override val items = listOf(
        TextItem("village", R.string.quest_caravanSiteType_village),
        TextItem("town", R.string.quest_caravanSiteType_town),
        TextItem("river", R.string.quest_caravanSiteType_river),
        TextItem("lake", R.string.quest_caravanSiteType_lake),
        TextItem("parking_lot", R.string.quest_caravanSiteType_parking_lot),
        TextItem("harbour", R.string.quest_caravanSiteType_harbour),
        TextItem("winery", R.string.quest_caravanSiteType_winery),
        TextItem("camp_site", R.string.quest_caravanSiteType_camp_site),
        TextItem("museum", R.string.quest_caravanSiteType_museum),
        TextItem("restaurant", R.string.quest_caravanSiteType_restaurant),
        TextItem("farm", R.string.quest_caravanSiteType_farm),
        TextItem("beach", R.string.quest_caravanSiteType_beach),
        TextItem("supermarket", R.string.quest_caravanSiteType_supermarket),
    )
}
