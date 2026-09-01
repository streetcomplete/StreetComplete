package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Composable

interface EmailAppLauncher {
    /** Open the default email app to send an email */
    fun compose(
        email: String,
        subject: String? = null,
        body: String? = null,
    )

    /** Return whether an email app is available */
    fun isAvailable(): Boolean
}

@Composable
expect fun rememberEmailAppLauncher(): EmailAppLauncher
