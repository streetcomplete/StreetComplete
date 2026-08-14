package de.westnordost.streetcomplete.quests.wheelchair_access

sealed interface WheelchairAccessToiletsPartAnswer {
    data object NoToilet : WheelchairAccessToiletsPartAnswer
}

data class WheelchairAccessToiletsPart(val access: WheelchairAccess) : WheelchairAccessToiletsPartAnswer
