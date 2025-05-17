package de.westnordost.streetcomplete.data.quest

sealed interface Countries

/** to be shown everywhere */
data object AllCountries : Countries
/** to be shown everywhere except in the given list of ISO 3166-1 alpha2 country codes */
data class AllCountriesExcept(val exceptions: List<String>) : Countries {
    constructor(vararg exceptions: String) : this(exceptions.toList())
}
/** to be shown only in the given list of ISO 3166-1 alpha2 country codes */
data class NoCountriesExcept(val exceptions: List<String>) : Countries {
    constructor(vararg exceptions: String) : this(exceptions.toList())
}
