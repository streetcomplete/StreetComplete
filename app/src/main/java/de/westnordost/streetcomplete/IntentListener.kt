package de.westnordost.streetcomplete

import android.content.Intent

interface IntentListener {
    fun onNewIntent(intent: Intent)
}