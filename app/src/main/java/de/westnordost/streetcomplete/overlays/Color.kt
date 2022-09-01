package de.westnordost.streetcomplete.overlays

/** Default and common colors for overlays. This palette is selected to be suitable for use by
 *  color-blind people too, that is
 *
 *  - Red-green color blindness: Deutan, Protan (~4.5% of population)
 *  - Blue-yellow color blindness (but not as good): Tritan (~0.01% of population)
 *
 *   See the palette here (update link if colors are updated!):
 *   https://davidmathlogic.com/colorblind/#%23444444-%23D00055-%231887E8-%2326B0F1-%2337DAF5-%2306CCC0-%2305A980-%23FD7E15-%23F7C204-%23B9F522
 *
 *   The palette is loosely based on Color Universal Design (CUD) by Masataka Okabe and Kei Ito's
 *   color palette (https://jfly.uni-koeln.de/color/), compare:
 *   https://davidmathlogic.com/colorblind/#%23000000-%230072B2-%2356B4E9-%23CC79A7-%23009E73-%23D55E00-%23E69F00-%23F0E442
 *
 *   However,
 *   - the colors have been made more vibrant
 *   - and a few added
 *   - balanced to not be too close to the colors used on the background map
 *
 *   Also, it has been made so that black and crimson stand out, because these two are reserved in
 *   all overlays as having a special meaning
 *  */
object Color {
    // colors with reserved meanings
    const val INVISIBLE = "#00000000" // "mapped separately" / "not relevant"
    const val BLACK = "#444444" // "no" / "does not exist"
    const val CRIMSON = "#D00055" // "not mapped" / "incomplete/invalid"

    // blue
    const val BLUE = "#007EEC"
    const val SKY = "#29AFEF"
    const val CYAN = "#37DAF5"
    // green-ish
    const val AQUAMARINE = "#03C3B8"
    const val TEAL = "#029E77"
    // orange-yellow
    const val ORANGE = "#FB892C"
    const val GOLD = "#F7C204"
    const val LIME = "#B9F522"

    // note that AQUAMARINE and TEAL look like SKY and BLUE for Blue-yellow color blind people
    // (~0.01% of population)
}
