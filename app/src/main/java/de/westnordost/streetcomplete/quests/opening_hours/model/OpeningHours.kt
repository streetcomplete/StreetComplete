package de.westnordost.streetcomplete.quests.opening_hours.model

sealed class OpeningHours {
    abstract override fun toString(): String
}

data class RegularOpeningHours(val openingMonths: List<OpeningMonths>): OpeningHours() {
    override fun toString(): String = openingMonths.joinToString(";")
}

object AlwaysOpen : OpeningHours() {
    override fun toString(): String = "24/7"
}

data class DescribedOpeningHours(val text:String): OpeningHours() {
    override fun toString(): String {
        val text = text.replace("\"","")
        return "\"$text\""
    }
}