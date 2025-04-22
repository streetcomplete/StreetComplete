package de.westnordost.streetcomplete.quests.lamp_mount

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem

class AddLampMountForm : AListQuestForm<LampMountAnswer>() {
    override val items: List<TextItem<LampMountAnswer>> = listOf(
        TextItem(LampMount("straight_mast"), R.string.quest_lampMount_straightMast),
        TextItem(LampMount("bent_mast"), R.string.quest_lampMount_bentMast),
        TextItem(LampMount("suspended"), R.string.quest_lampMount_suspended),
        TextItem(LampMount("angled_mast"), R.string.quest_lampMount_angledMast),
        TextItem(LampMount("high_mast"), R.string.quest_lampMount_highMast),
        TextItem(LampMount("bollard"), R.string.quest_lampMount_bollard),
        TextItem(LampMount("wall"), R.string.quest_lampMount_wall),
        TextItem(Support("ceiling"), R.string.quest_lampMount_ceiling),
        TextItem(Support("street_furniture:transit_shelter"), R.string.quest_lampMount_transitShelter),
    )
}
