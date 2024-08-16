package de.westnordost.streetcomplete.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/* Colors as they could be found on (illustrations of) traffic signs. */
val TrafficRed = Color(0xffc1121c)
val TrafficBlue = Color(0xff2255bb)
val TrafficGreen = Color(0xff008351)
val TrafficYellow = Color(0xffffd520)
val TrafficBrown = Color(0xff73411f)
val TrafficWhite = Color(0xffffffff)
val TrafficBlack = Color(0xff000000)
val TrafficGrayA = Color(0xff8e9291)
val TrafficGrayB = Color(0xff4f5250)

/* Colors for the teams in team mode.  */
val TeamColors = arrayOf(
    Color(0xfff44336),
    Color(0xff529add),
    Color(0xFFFBC02D),
    Color(0xffca72e2),
    Color(0xff9bbe55),
    Color(0xfff4900c),
    Color(0xff9aa0ad),
    Color(0xff6390a0),
    Color(0xffa07a43),
    Color(0xff494EAD),
    Color(0xffAA335D),
    Color(0xff655555),
)

val White = Color(0xffffffff)

val GrassGreen = Color(0xff80b158)
val GrassGray = Color(0xff888888)
val LeafGreen = Color(0xff006a00)

val LightColors = lightColors(
    primary = Color(0xff4141ba),
    primaryVariant = Color(0xff3939a3),
    secondary = Color(0xffD14000),
    secondaryVariant = Color(0xffF44336),
    onPrimary = Color.White,
    onSecondary = Color.White
)

val DarkColors = darkColors(
    primary = Color(0xff4141ba),
    primaryVariant = Color(0xff3939a3),
    secondary = Color(0xffff6600),
    secondaryVariant = Color(0xffF44336),
    onPrimary = Color.White,
    onSecondary = Color.White
)

val Colors.selectionBackground @ReadOnlyComposable @Composable get() =
    MaterialTheme.colors.secondary.copy(alpha = 0.5f)

val Colors.surfaceContainer @ReadOnlyComposable @Composable get() =
    if (isLight) Color(0xffdddddd) else Color(0xff222222)

// use lighter tones (200) for increased contrast with dark background

val Colors.logVerbose @ReadOnlyComposable @Composable get() =
    if (isLight) Color(0xff666666) else Color(0xff999999)

val Colors.logDebug @ReadOnlyComposable @Composable get() =
    if (isLight) Color(0xff2196f3) else Color(0xff90caf9)

val Colors.logInfo @ReadOnlyComposable @Composable get() =
    if (isLight) Color(0xff4caf50) else Color(0xffa5d6a7)

val Colors.logWarning @ReadOnlyComposable @Composable get() =
    if (isLight) Color(0xffff9800) else Color(0xffffcc80)

val Colors.logError @ReadOnlyComposable @Composable get() =
    if (isLight) Color(0xfff44336) else Color(0xffef9a9a)
