package de.westnordost.streetcomplete.quests.parking_access

import de.westnordost.streetcomplete.quests.parking_access.ParkingAccess.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_access_customers
import de.westnordost.streetcomplete.resources.quest_access_private
import de.westnordost.streetcomplete.resources.quest_access_yes
import org.jetbrains.compose.resources.StringResource

enum class ParkingAccess(val osmValue: String) {
    YES("yes"),
    CUSTOMERS("customers"),
    PRIVATE("private"),
}

val ParkingAccess.text: StringResource get() = when (this) {
    YES -> Res.string.quest_access_yes
    CUSTOMERS -> Res.string.quest_access_customers
    PRIVATE -> Res.string.quest_access_private
}
