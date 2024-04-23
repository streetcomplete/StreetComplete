package de.westnordost.streetcomplete.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

val Typography = Typography()

val Typography.title get() = h6.copy(
    fontWeight = FontWeight.Bold,
    fontFamily = FontFamily(Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Bold))
)

val Typography.headline get() = h4.copy(fontWeight = FontWeight.Bold)
val Typography.headlineSmall get() = h5.copy(fontWeight = FontWeight.Bold)
