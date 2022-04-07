package de.westnordost.streetcomplete.quests.entrance_reference

sealed interface EntranceAnswer

data class ReferenceCode(val referenceCode: String) : EntranceAnswer
data class ReferenceCodeAndFlatRange(val referenceCode: String, val flatRange: String) : EntranceAnswer
data class FlatRange(val flatRange: String) : EntranceAnswer
object Unsigned : EntranceAnswer
