package de.westnordost.streetcomplete.osm

/** Returns true if value is from some well defined set of values
 *  Return false otherwise
 *  Note that it returns false where StreetComplete is using a limited subset of values
 *  even if tag is effectively freeform for OSM mappers in general
 *  For example building=* is not considered as freeform.
 *  Note that freeform tags where SC is using dedicated subset of values but tag is completely freeform
 *  for example source:maxheight where sole value set by StreetComplete is "ARCore"
 * */
fun isTagValueLinkableToWiki(key: String, value: String): Boolean {
    if (value in listOf(null, "no", "yes", "only")) {
        // sometimes page may exist, sometimes it does not
        return false
    }
    if ( key == "fire_hydrant:type") {
        return false
    }
    if (key in listOf("crossing:barrier", "bicycle_rental", "roof:shape", "material", "royal_cypher", "camera:type",
            "bollard", "board_type", "cycle_barrier", "bicycle_parking", "location", "stile", "shoulder",
            "toilets:wheelchair", "ramp:wheelchair", "smoking")) {
        // in this cases values are not getting own wiki pages, but it can change in future
        return false
    }
    if (key.startsWith("recycling:") || key.startsWith("parking:")
        || key.startsWith("cycleway:") || key.startsWith("footway:")) {
        // tag families with manu, many subvariants
        return false
    }

    if (";" in value) {
        // sport=soccer;volleyball is fully valid but given free reordering is effectively freeform
        // similarly produce tag fits here
        return false
    }

    // most have own syntax and limitations obeyed by SC
    if (key in listOf("name", "int_name", "ref",
            "addr:flats", "addr:housenumber", "addr:street", "addr:place", "addr:block_number", "addr:streetnumber",
            "addr:conscriptionnumber", "addr:housename",
            "building:levels", "roof:levels", "level",
            "collection_times", "opening_hours", "opening_date", "check_date",
            "fire_hydrant:diameter", "maxheight", "width", "cycleway:width",
            "maxspeed", "maxspeed:advisory", "maxstay",
            "maxweight", "maxweightrating", "maxaxleload", "maxbogieweight",
            "maxspeed:type", // maybe not strictly true, but very close to being freeform
            "capacity", "step_count",
            "lanes", "lanes:forward", "lanes:backward", "lanes:both_ways",
            "turn:lanes:both_ways", "turn:lanes", "turn:lanes:forward", "turn:lanes:backward",
            "operator", // technically not fully, but does not make sense to list all that autocomplete values
            "brand",
        )) {
        return false
    }
    if (SURVEY_MARK_KEY in key) {
        return false
    }
    if (key.endsWith(":note")) {
        return false
    }
    if (key.endsWith(":conditional")) {
        return false
    }
    if (key.endsWith(":wikidata")) {
        return false
    }
    if (key.endsWith(":wikipedia")) {
        return false
    }
    if (key.startsWith("lanes:")) {
        return false
    }
    if (key.startsWith("name:")) {
        return false
    }
    if (key.startsWith("source:")) {
        return false
    }
    return true
}
