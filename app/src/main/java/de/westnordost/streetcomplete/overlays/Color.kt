package de.westnordost.streetcomplete.overlays

/** Default and common colors for overlays. This palette is selected to be suitable for use by
 *  color-blind people too, that is
 *
 *  - Red-green color blindness: Deutan, Protan (~4.5% of population)
 *  - Blue-yellow color blindness (but not as good): Tritan (~0.01% of population)
 *
 *   See the palette here (update link if colors are updated!):
 *   https://davidmathlogic.com/colorblind/#%23444444-%23DC013B-%23007EEC-%2329AFEF-%2306CCC0-%2305A980-%23FD7E15-%23F7CE04-%23B9F522
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
 *   all overlays as having a special meaning: crimson for "data is unspecified" and black for
 *   "(feature) does not exist" (e.g. not lit, no sidewalk, ...)
 *  */
object Color {
    const val INVISIBLE = "#00000000"
    const val BLACK = "#444444"
    const val CRIMSON = "#DC013B"
    // blue
    const val BLUE = "#007EEC"
    const val SKY = "#29AFEF"
    // orange-yellow
    const val ORANGE = "#FD7E15"
    const val GOLD = "#F7CE04"
    const val LIME = "#B9F522"

    // these colors are quite close to the blue colors for blue-yellow blindness, Teal is
    // additionally quite close to crimson for red-blue blindness
    const val AQUAMARINE = "#06CCC0"
    const val TEAL = "#05A980"
}
