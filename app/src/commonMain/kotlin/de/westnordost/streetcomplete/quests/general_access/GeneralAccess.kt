package de.westnordost.streetcomplete.quests.general_access

import de.westnordost.streetcomplete.quests.general_access.GeneralAccess.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.StringResource

enum class GeneralAccess(val osmValue: String) {
    YES("yes"),
    CUSTOMERS("customers"),
    PRIVATE("private"),
}

val GeneralAccess.text: StringResource get() = when (this) {
    YES -> Res.string.quest_access_yes
    CUSTOMERS -> Res.string.quest_access_customers
    PRIVATE -> Res.string.quest_access_private
}
