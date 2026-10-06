package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Composable

@Composable
actual fun rememberEmailAppLauncher(): EmailAppLauncher = object : EmailAppLauncher {
    override fun isAvailable(): Boolean = false
    override fun compose(email: String, subject: String?, body: String?) {}
}
