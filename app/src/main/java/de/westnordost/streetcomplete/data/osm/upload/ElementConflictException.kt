package de.westnordost.streetcomplete.data.osm.upload

open class ElementConflictException @JvmOverloads constructor(
    message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)

class ElementDeletedException @JvmOverloads constructor(
    message: String? = null, cause: Throwable? = null) : ElementConflictException(message, cause)
