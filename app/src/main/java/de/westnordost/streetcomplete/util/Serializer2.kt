package de.westnordost.streetcomplete.util

interface Serializer2 {
    fun <T> encode(value: T): String
    fun <T> decode(string: String): T
}
