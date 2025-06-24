package de.westnordost.streetcomplete.data.overlays

import androidx.compose.ui.graphics.Color

/** Default and common colors for overlays. This palette is selected to be suitable for use by
 *  color-blind people too, that is
 *
 *  - Red-green color blindness: Deutan, Protan (~4.5% of population)
 *  - Blue-yellow color blindness (but not as good): Tritan (~0.01% of population)
 *
 *   See the palette here (update link if colors are updated!):
 *   https://davidmathlogic.com/colorblind/#%230f0f0f-%23FF0000-%23BF39A5-%231A87E6-%232FACE8-%2330D4EE-%2310C1B8-%230DA082-%23F37D1E-%23EEBD0D-%23B6EF28
 *
 *   The palette is loosely based on Color Universal Design (CUD) by Masataka Okabe and Kei Ito's
 *   color palette (https://jfly.uni-koeln.de/color/), compare:
 *   https://davidmathlogic.com/colorblind/#%23000000-%230072B2-%2356B4E9-%23CC79A7-%23009E73-%23D55E00-%23E69F00-%23F0E442-%23DDDDDD
 *
 *   However,
 *   - the colors have been made more vibrant
 *   - and a few added
 *   - balanced to not be too close to the colors used on the background map
 *
 *   Also, it has been made so that black and red stand out, because these two are reserved in
 *   all overlays as having a special meaning
 */
object OverlayColor {
    // colors with reserved meanings
    val Invisible = Color(0x00000000) // "mapped separately" / "not relevant"
    val Black = Color(0xff0f0f0f) // "no" / "does not exist"
    val Red = Color(0xffFF0000) // "not mapped" / "incomplete/invalid" / "data missing" / "outdated"

    // blue-ish
    val Purple = Color(0xffBF39A5)
    val Blue = Color(0xff1A87E6)
    val Sky = Color(0xff2FACE8)
    val Cyan = Color(0xff30D4EE)
    // green-ish
    val Aquamarine = Color(0xff10C1B8)
    val Teal = Color(0xff0DA082)
    // orange-yellow
    val Orange = Color(0xffF37D1E)
    val Gold = Color(0xffEEBD0D)
    val Lime = Color(0xffB6EF28)

    // note that AQUAMARINE and TEAL look like SKY and BLUE for Blue-yellow color blind people
    // (~0.01% of population)
}
