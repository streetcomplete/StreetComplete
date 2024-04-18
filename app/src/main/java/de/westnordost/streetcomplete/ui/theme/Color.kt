package de.westnordost.streetcomplete.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/* Contains colors independent of (dark/light) theme.
 *
 *  If the color (could) change between themes, define them in AppColors */
object Color {
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
}

val LightColors = AppColors(
    material = lightColors(
        primary = Color(0xff4141ba),
        primaryVariant = Color(0xff3939a3),
        secondary = Color(0xffD14000),
        secondaryVariant = Color(0xffF44336),
        onPrimary = Color.White,
        onSecondary = Color.White
    ),
    logVerbose = Color(0xff666666),
    logDebug = Color(0xff2196f3),
    logInfo = Color(0xff4caf50),
    logWarning = Color(0xffff9800),
    logError = Color(0xfff44336),
)

val DarkColors = AppColors(
    material = darkColors(
        primary = Color(0xff4141ba),
        primaryVariant = Color(0xff3939a3),
        secondary = Color(0xffff6600),
        secondaryVariant = Color(0xffF44336),
        onPrimary = Color.White,
        onSecondary = Color.White
    ),
    // use lighter tones (200) for increased contrast with dark background
    logVerbose = Color(0xff999999),
    logDebug = Color(0xff90caf9),
    logInfo = Color(0xffa5d6a7),
    logWarning = Color(0xffffcc80),
    logError = Color(0xffef9a9a),
)

/* Colors specific to this app */
@Immutable
data class AppColors(
    val material: Colors,
    val logVerbose: Color,
    val logDebug: Color,
    val logInfo: Color,
    val logWarning: Color,
    val logError: Color,
)

// custom app colors are organized like suggested in
// https://gustav-karlsson.medium.com/extending-the-jetpack-compose-material-theme-with-more-colors-e1b849390d50
// This is slightly different than what can be found in an official example app on
// https://github.com/android/compose-samples/tree/main/Jetsnack
// but I am not sure whether for the latter, colors are automatically chosen by default for
// composables.
