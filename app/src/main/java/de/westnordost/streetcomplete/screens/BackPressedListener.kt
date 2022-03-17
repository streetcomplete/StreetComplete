package de.westnordost.streetcomplete.screens

interface BackPressedListener {
    /** Return true to consume the event */
    fun onBackPressed(): Boolean
}
