package de.westnordost.streetcomplete.quests.wheelchair_access

enum class WheelchairAccess(val osmValue: String, var updatedDescriptions: Map<String, String>? = null) {
    YES("yes"),
    LIMITED("limited"),
    NO("no"),
}
