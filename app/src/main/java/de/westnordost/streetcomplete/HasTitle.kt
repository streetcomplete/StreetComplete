package de.westnordost.streetcomplete

interface HasTitle {
    val title: String
    val subtitle: String? get() = null
}
