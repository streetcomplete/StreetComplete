package de.westnordost.streetcomplete.osm.cycleway_separate

enum class SeparateCycleway {
    /** No bicycles here */
    NONE,
    /** Bicycles allowed on footway / bridleway / path */
    ALLOWED,
    /** Unspecific value: This footway / bridleway / path is not designated for cyclists. I.e. it is
     *  either NONE or ALLOWED. This value is never output by the parser. */
    NON_DESIGNATED,
    /** Designated but not segregated from footway / bridleway mapped on same way */
    NON_SEGREGATED,
    /** Designated and segregated from footway / bridleway mapped on same way */
    SEGREGATED,
    /** This way is a cycleway only, no footway or bridleway mapped on the same way */
    EXCLUSIVE,
    /** This way is a cycleway only, however it has a sidewalk mapped on the same way, like some
     *  sort of tiny road for cyclists only */
    EXCLUSIVE_WITH_SIDEWALK
}
