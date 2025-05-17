package de.westnordost.streetcomplete.util.ktx

fun <X, Y> Map<X, Y>.containsAll(other: Map<X, Y>) = other.all { this[it.key] == it.value }
