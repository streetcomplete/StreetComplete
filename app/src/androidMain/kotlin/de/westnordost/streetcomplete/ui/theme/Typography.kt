package de.westnordost.streetcomplete.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

private val material2 = Typography()

val Typography = Typography(
    h4 = material2.h4.copy(fontWeight = FontWeight.Bold),
    h5 = material2.h5.copy(fontWeight = FontWeight.Bold),
    h6 = material2.h6.copy(
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily(Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Bold))
    ),
    subtitle1 = material2.subtitle1.copy(
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily(Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Bold))
    ),
    subtitle2 = material2.subtitle2.copy(
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily(Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Bold))
    )
)

// for easier conversion to M3
val Typography.headlineLarge get() = h4
val Typography.headlineSmall get() = h5
val Typography.titleLarge get() = h6
val Typography.titleMedium get() = subtitle1
val Typography.titleSmall get() = subtitle2
