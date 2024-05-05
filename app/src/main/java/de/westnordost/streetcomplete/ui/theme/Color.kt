package de.westnordost.streetcomplete.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
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
val Team0 = Color(0xfff44336)
val Team1 = Color(0xff529add)
val Team2 = Color(0xffffdd55)
val Team3 = Color(0xffca72e2)
val Team4 = Color(0xff9bbe55)
val Team5 = Color(0xfff4900c)
val Team6 = Color(0xff9aa0ad)
val Team7 = Color(0xff6390a0)
val Team8 = Color(0xffa07a43)
val Team9 = Color(0xff494EAD)
val Team10 = Color(0xffAA335D)
val Team11 = Color(0xff655555)

val White = Color(0xffffffff)

val GrassGreen = Color(0xff80b158)
val GrassGray = Color(0xffb1b1b1)
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

val Colors.hint @Composable get() =
    if (isLight) Color(0xff666666) else Color(0xff999999)

val Colors.surfaceContainer @Composable get() =
    if (isLight) Color(0xffdddddd) else Color(0xff222222)

// use lighter tones (200) for increased contrast with dark background

val Colors.logVerbose @Composable get() =
    if (isLight) Color(0xff666666) else Color(0xff999999)

val Colors.logDebug @Composable get() =
    if (isLight) Color(0xff2196f3) else Color(0xff90caf9)

val Colors.logInfo @Composable get() =
    if (isLight) Color(0xff4caf50) else Color(0xffa5d6a7)

val Colors.logWarning @Composable get() =
    if (isLight) Color(0xffff9800) else Color(0xffffcc80)

val Colors.logError @Composable get() =
    if (isLight) Color(0xfff44336) else Color(0xffef9a9a)
