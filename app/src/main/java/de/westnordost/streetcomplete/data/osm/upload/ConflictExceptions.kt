package de.westnordost.streetcomplete.data.osm.upload

open class ConflictException @JvmOverloads constructor(
    message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)

class ChangesetConflictException @JvmOverloads constructor(
    message: String? = null, cause: Throwable? = null) : ConflictException(message, cause)

open class ElementConflictException @JvmOverloads constructor(
    message: String? = null, cause: Throwable? = null) : ConflictException(message, cause)

/** Element conflict that concern all quests that have something to do with this element */
open class ElementIncompatibleException @JvmOverloads constructor(
    message: String? = null, cause: Throwable? = null) : ElementConflictException(message, cause)

class ElementDeletedException @JvmOverloads constructor(
    message: String? = null, cause: Throwable? = null) : ElementIncompatibleException(message, cause)
