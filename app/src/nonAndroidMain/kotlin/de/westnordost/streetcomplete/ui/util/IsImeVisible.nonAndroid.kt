package de.westnordost.streetcomplete.ui.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity

@Composable
actual fun isImeVisible(): Boolean = WindowInsets.ime.getBottom(LocalDensity.current) > 0
