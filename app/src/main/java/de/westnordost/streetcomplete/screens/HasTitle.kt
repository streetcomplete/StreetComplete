package de.westnordost.streetcomplete.screens

interface HasTitle {
    val title: String
    val subtitle: String? get() = null
}
