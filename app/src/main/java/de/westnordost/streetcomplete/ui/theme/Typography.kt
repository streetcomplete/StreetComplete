package de.westnordost.streetcomplete.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

val Typography = Typography()

val Typography.titleLarge get() = h6.copy(
    fontWeight = FontWeight.Bold,
    fontFamily = FontFamily(Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Bold))
)
val Typography.titleMedium get() = subtitle1.copy(
    fontWeight = FontWeight.Bold,
    fontFamily = FontFamily(Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Bold))
)
val Typography.titleSmall get() = subtitle2.copy(
    fontWeight = FontWeight.Bold,
    fontFamily = FontFamily(Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Bold))
)

val Typography.headlineLarge get() = h4.copy(fontWeight = FontWeight.Bold)
val Typography.headlineSmall get() = h5.copy(fontWeight = FontWeight.Bold)
