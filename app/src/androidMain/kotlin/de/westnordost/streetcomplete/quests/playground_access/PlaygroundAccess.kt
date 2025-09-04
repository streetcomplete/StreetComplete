package de.westnordost.streetcomplete.quests.playground_access

import de.westnordost.streetcomplete.quests.playground_access.PlaygroundAccess.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_access_customers
import de.westnordost.streetcomplete.resources.quest_access_private
import de.westnordost.streetcomplete.resources.quest_access_yes
import org.jetbrains.compose.resources.StringResource

enum class PlaygroundAccess(val osmValue: String) {
    YES("yes"),
    CUSTOMERS("customers"),
    PRIVATE("private"),
}

val PlaygroundAccess.text: StringResource get() = when (this) {
    YES -> Res.string.quest_access_yes
    CUSTOMERS -> Res.string.quest_access_customers
    PRIVATE -> Res.string.quest_access_private
}
