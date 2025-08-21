package de.westnordost.streetcomplete.quests.building_entrance_reference

import kotlinx.serialization.Serializable

sealed interface EntranceReferenceAnswer {
    data object NotSigned : EntranceReferenceAnswer
}

@Serializable
sealed interface EntranceReference : EntranceReferenceAnswer {
    fun isComplete(): Boolean
}

@Serializable
data class ReferenceCode(val value: String) : EntranceReference {
    override fun isComplete(): Boolean = value.isNotEmpty()
}
@Serializable
data class FlatRange(val start: String, val end: String) : EntranceReference {
    override fun isComplete(): Boolean = start.isNotEmpty() && end.isNotEmpty()
}
@Serializable
data class ReferenceCodeAndFlatRange(
    val referenceCode: ReferenceCode,
    val flatRange: FlatRange
) : EntranceReference {
    override fun isComplete(): Boolean = referenceCode.isComplete() && flatRange.isComplete()
}

