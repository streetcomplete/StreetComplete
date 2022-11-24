package de.westnordost.streetcomplete.osm.cycleway_separate

enum class SeparateCycleway {
    /** No (designated) cycleway */
    NONE,
    /** Bicycles allowed on cycleway */
    ALLOWED,
    /** Not segregated from footway or bridleway mapped on same way */
    NON_SEGREGATED,
    /** Segregated from footway or bridleway mapped on same way */
    SEGREGATED,
    /** This way is a cycleway only, no footway or bridleway mapped on the same way */
    EXCLUSIVE,
    /** This way is a cycleway only, however it has a sidewalk mapped on the same way, like some
     *  sort of tiny road for cyclists only */
    WITH_SIDEWALK
}
