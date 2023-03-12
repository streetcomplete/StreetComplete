package de.westnordost.streetcomplete.quests.wheelchair_access

sealed interface WheelchairAccessToiletsAnswer
data class WheelchairAccessToilets(val access: WheelchairAccess) : WheelchairAccessToiletsAnswer
object NoToilet : WheelchairAccessToiletsAnswer
