package de.westnordost.streetcomplete.data.quest



val KEYS_THAT_SHOULD_NOT_BE_REMOVED_WHEN_SHOP_IS_REPLACED = listOf(
    "landuse", "historic",
    // building/simple 3d building mapping
    "building", "man_made", "building:.*", "roof:.*",
    // any address
    "addr:.*",
    // shop can at the same time be an outline in indoor mapping
    "level", "level:ref", "indoor", "room",
    // geometry
    "layer", "ele", "height", "area", "is_in",
    // notes and fixmes
    "FIXME", "fixme", "note",
    // reference tags that are specific to the location and are unlikely to change when a business changes
    "ref:bag", "ref:GB:uprn", "ref:linz:building_id", "ref:linz:topo50_id", "ref:ruian:.*", "ref:UrbIS"
).map { it.toRegex() }
