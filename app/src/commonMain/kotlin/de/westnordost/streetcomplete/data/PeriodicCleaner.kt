package de.westnordost.streetcomplete.data

/** Enqueues a background job to do cleanup of old data some time in a day or so */
interface PeriodicCleaner {
    fun enqueue()
}
