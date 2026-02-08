package de.westnordost.streetcomplete.quests.ferry

import androidx.annotation.StringRes
import de.westnordost.streetcomplete.R

enum class FerryBicycleAccess {
    ALLOWED,
    NOT_ALLOWED,
    NOT_SIGNED
}

@get:StringRes
val FerryBicycleAccess.text: Int
    get() = when (this) {
        FerryBicycleAccess.ALLOWED ->
            R.string.quest_ferry_bicycle_allowed
        FerryBicycleAccess.NOT_ALLOWED ->
            R.string.quest_ferry_bicycle_not_allowed
        FerryBicycleAccess.NOT_SIGNED ->
            R.string.quest_ferry_bicycle_not_signed
    }
