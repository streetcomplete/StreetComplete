package de.westnordost.streetcomplete.data.user.achievements

/** Achievements granted by contributing edits to OSM */
enum class EditTypeAchievement(val id: String) {
    RARE("rare"),
    CAR("car"),
    VEG("veg"),
    PEDESTRIAN("pedestrian"),
    BUILDING("building"),
    POSTMAN("postman"),
    BLIND("blind"),
    WHEELCHAIR("wheelchair"),
    BICYCLIST("bicyclist"),
    CITIZEN("citizen"),
    OUTDOORS("outdoors"),
    LIFESAVER("lifesaver"),
}
