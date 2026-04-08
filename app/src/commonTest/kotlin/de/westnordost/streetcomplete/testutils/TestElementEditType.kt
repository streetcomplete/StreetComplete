package de.westnordost.streetcomplete.testutils

import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.resources.*

object TestElementEditType : ElementEditType {
    override val changesetComment = "test element edit type"
    override val icon = 0
    override val title = Res.string.quest_address_title
    override val wikiLink = null
    override val achievements = emptyList<EditTypeAchievement>()
}
