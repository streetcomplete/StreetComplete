package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable

/** Whether the software keyboard (IME) is currently visible. */
@Composable
// TODO CMP: this is necessary as long as https://youtrack.jetbrains.com/issue/CMP-9906/Commonize-WindowInsets.isVisible-functions is not implemented
expect fun isImeVisible(): Boolean
