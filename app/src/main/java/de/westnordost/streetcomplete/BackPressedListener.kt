package de.westnordost.streetcomplete

interface BackPressedListener {
    /** Return true to consume the event */
    fun onBackPressed(): Boolean
}
