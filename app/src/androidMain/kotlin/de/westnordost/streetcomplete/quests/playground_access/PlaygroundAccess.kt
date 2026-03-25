package de.westnordost.streetcomplete.quests.playground_access

import de.westnordost.streetcomplete.quests.playground_access.PlaygroundAccess.*
import de.westnordost.streetcomplete.resources.*
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
