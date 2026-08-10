package de.westnordost.streetcomplete.quests.building_entrance_reference

import kotlinx.serialization.Serializable

@Serializable
sealed interface EntranceReferenceAnswer {
    data object NotSigned : EntranceReferenceAnswer
}

@Serializable
sealed interface EntranceReference : EntranceReferenceAnswer {
    fun isComplete(): Boolean
    fun clear(): EntranceReference
}

@Serializable
data class ReferenceCode(val value: String) : EntranceReference {
    override fun isComplete(): Boolean = value.isNotBlank()
    override fun clear() = ReferenceCode("")
}
@Serializable
data class FlatRange(val start: String, val end: String) : EntranceReference {
    override fun isComplete(): Boolean = start.isNotBlank() && end.isNotBlank()
    override fun clear() = FlatRange("", "")
}
@Serializable
data class ReferenceCodeAndFlatRange(
    val referenceCode: ReferenceCode,
    val flatRange: FlatRange
) : EntranceReference {
    override fun isComplete(): Boolean = referenceCode.isComplete() && flatRange.isComplete()
    override fun clear() = ReferenceCodeAndFlatRange(ReferenceCode(""), FlatRange("", ""))
}
