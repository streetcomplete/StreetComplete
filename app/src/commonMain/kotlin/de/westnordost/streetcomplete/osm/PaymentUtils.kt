package de.westnordost.streetcomplete.osm

val PLACES_THAT_REQUIRE_PAYMENT_FILTER = """
    (
        (shop and shop !~ no|vacant|mall)
        or amenity ~ ${arrayOf(
            "bar", "cafe", "fast_food", "ice_cream", "pub", "biergarten", "restaurant", "fuel",
            "cinema", "nightclub", "planetarium", "theatre", "internet_cafe", "car_wash",
            "pharmacy", "telephone", "vending_machine", "luggage_locker"
        ).joinToString("|")}
        or leisure ~ ${arrayOf(
            "adult_gaming_centre", "amusement_arcade", "bowling_alley", "escape_game",
            "miniature_golf","sauna", "trampoline_park", "tanning_salon"
        ).joinToString("|")}
        or craft ~ ${arrayOf(
            "carpenter", "shoemaker", "tailor", "photographer", "dressmaker",
            "electronics_repair", "key_cutter", "stonemason"
        ).joinToString("|")}
        or tourism ~ ${arrayOf(
            "theme_park", "hotel", "hostel", "motel", "guest_house",
            "apartment", "camp_site", "alpine_hut"
        ).joinToString("|")}
        or tourism ~ ${arrayOf(
            "museum", "gallery", "zoo", "aquarium"
        ).joinToString("|")} and fee = yes
    )
    and fee != no
"""

