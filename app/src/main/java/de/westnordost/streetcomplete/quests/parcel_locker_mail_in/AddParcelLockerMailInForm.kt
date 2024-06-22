package de.westnordost.streetcomplete.quests.parcel_locker_mail_in

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddParcelLockerMailInForm : AbstractOsmQuestForm<ParcelLockerMailIn>() {
    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(ParcelLockerMailIn.NO) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(ParcelLockerMailIn.YES ) },
    )

    override val otherAnswers get() = listOf(
        AnswerItem(R.string.quest_wheelchairAccessPat_noToilet) {
            applyAnswer(ParcelLockerMailIn.RETURNS_ONLY)
        }
    )
}
