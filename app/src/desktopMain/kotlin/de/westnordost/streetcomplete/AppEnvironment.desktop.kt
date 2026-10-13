package de.westnordost.streetcomplete

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.LayoutDirection
import de.westnordost.streetcomplete.data.preferences.Theme
import de.westnordost.streetcomplete.ui.AppLocale
import de.westnordost.streetcomplete.ui.LocalAppLocale
import java.awt.ComponentOrientation

@Composable
actual fun AppEnvironment(locale: Locale?, theme: Theme, keepScreenOn: Boolean, content: @Composable () -> Unit) {
    val effectiveLocale = locale ?: Locale.current
    val direction = if (ComponentOrientation.getOrientation(effectiveLocale.platformLocale).isLeftToRight) {
        LayoutDirection.Ltr
    } else {
        LayoutDirection.Rtl
    }
    CompositionLocalProvider(
        LocalAppLocale provides AppLocale(locale, LocaleList.current),
        LocalLayoutDirection provides direction,
        content = content,
    )
}
