package de.westnordost.streetcomplete.overlays

/** Default and common colors for overlays. This palette is selected to be suitable for use by
 *  color-blind people too, that is
 *
 *  - Red-green color blindness: Deutan, Protan (~4.5% of population)
 *  - Blue-yellow color blindness (but not as good): Tritan (~0.01% of population)
 *
 *   See the palette here (update link if colors are updated!):
 *   https://davidmathlogic.com/colorblind/#%23444444-%23FF0000-%231A87E6-%232FACE8-%2330D4EE-%2310C1B8-%230DA082-%23F37D1E-%23EEBD0D-%23B6EF28
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
    const val BLACK = "#0f0f0f" // "no" / "does not exist"
    private const val RED = "#FF0000" // reserved
    const val DATA_REQUESTED = RED // "not mapped" / "incomplete/invalid" / "data missing" / "outdated"

    // blue
    const val BLUE = "#1A87E6"
    const val SKY = "#2FACE8"
    const val CYAN = "#30D4EE"
    // green-ish
    const val AQUAMARINE = "#10C1B8"
    const val TEAL = "#0DA082"
    // orange-yellow
    const val ORANGE = "#F37D1E"
    const val GOLD = "#EEBD0D"
    const val LIME = "#B6EF28"

    // note that AQUAMARINE and TEAL look like SKY and BLUE for Blue-yellow color blind people
    // (~0.01% of population)
}
