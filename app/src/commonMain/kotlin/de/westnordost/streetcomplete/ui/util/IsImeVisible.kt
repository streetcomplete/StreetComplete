package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable

/** Whether the software keyboard (IME) is currently visible. */
@Composable
expect fun isImeVisible(): Boolean
