package de.westnordost.streetcomplete.testutils

import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement

object TestElementEditType : ElementEditType {
    override val changesetComment = "test element edit type"
    override val icon = 0
    override val title = 0
    override val wikiLink: String? = null
    override val achievements: List<EditTypeAchievement> = emptyList()
}
