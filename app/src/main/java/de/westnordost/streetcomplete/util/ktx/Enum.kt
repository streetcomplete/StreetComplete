package de.westnordost.streetcomplete.util.ktx

inline fun <reified E : Enum<E>> valueOfOrNull(str: String): E? =
    enumValues<E>().firstOrNull { str == it.name }
