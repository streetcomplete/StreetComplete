package de.westnordost.streetcomplete.quests.post_office

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem

class AddPostOfficeTypeForm : AListQuestForm<String>() {
    override val items = listOf(
        TextItem("bureau", R.string.quest_postOffice_bureau),
        TextItem("post_annex", R.string.quest_postOffice_postAnnex),
        TextItem("post_partner", R.string.quest_postOffice_postPartner),
    )
}
