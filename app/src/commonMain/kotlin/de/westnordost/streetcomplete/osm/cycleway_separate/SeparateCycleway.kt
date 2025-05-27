package de.westnordost.streetcomplete.osm.cycleway_separate

enum class SeparateCycleway {
    /** Just a path, not designated to anyone in particular */
    PATH,
    /** No bicycles here */
    NOT_ALLOWED,
    /** Bicycles allowed on footway */
    ALLOWED_ON_FOOTWAY,
    /** Unspecific value: This footway is not designated for cyclists. I.e. it is
     *  either NOT_ALLOWED or ALLOWED_ON_FOOTWAY (or not specified) */
    NON_DESIGNATED_ON_FOOTWAY,
    /** Designated but not segregated from footway mapped on same way */
    NON_SEGREGATED,
    /** Designated and segregated from footway mapped on same way */
    SEGREGATED,
    /** This way is a cycleway only, no footway mapped on the same way */
    EXCLUSIVE,
    /** This way is a cycleway only, however it has a sidewalk mapped on the same way, like some
     *  sort of tiny road for cyclists only */
    EXCLUSIVE_WITH_SIDEWALK
}
