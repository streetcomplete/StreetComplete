package de.westnordost.streetcomplete.quests.board_name

import de.westnordost.streetcomplete.osm.localized_name.LocalizedName

sealed interface BoardNameAnswer

data object NoBoardName : BoardNameAnswer
data class BoardName(val localizedNames: List<LocalizedName>) : BoardNameAnswer
