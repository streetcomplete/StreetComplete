package de.westnordost.streetcomplete.screens.main.map

import androidx.annotation.DrawableRes
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.getNameLabel

@DrawableRes fun getPinIcon(map: Map<String, String>): Int? {
    when (map["amenity"]) {
        "atm" -> return R.drawable.ic_pin_money
        "bench" -> return R.drawable.ic_pin_bench
        "bicycle_parking" -> return R.drawable.ic_pin_bicycle_parking
        "charging_station" -> return R.drawable.ic_pin_car_charger
        "clock" -> return R.drawable.ic_pin_clock
        "drinking_water" -> return R.drawable.ic_pin_water
        "motorcycle_parking" -> return R.drawable.ic_pin_motorcycle_parking
        "parking" -> return R.drawable.ic_pin_parking
        "post_box" -> return R.drawable.ic_pin_mail
        "public_bookcase" -> return R.drawable.ic_pin_book
        "recycling" -> return R.drawable.ic_pin_recycling_container
        "telephone" -> return R.drawable.ic_pin_phone
        "toilets" -> return R.drawable.ic_pin_toilets
        "waste_basket" -> return R.drawable.ic_pin_bin
    }
    when (map["barrier"]) {
        "bollard" -> return R.drawable.ic_pin_bollard
    }
    when (map["emergency"]) {
        "defibrillator" -> return R.drawable.ic_pin_defibrillator
        "fire_hydrant" -> return R.drawable.ic_pin_fire_hydrant
        "phone" -> return R.drawable.ic_pin_phone
    }
    when (map["highway"]) {
        "crossing" -> {
            if (map["crossing"] == "traffic_signals")
                return R.drawable.ic_pin_pedestrian_traffic_light
            else
                return R.drawable.ic_pin_crossing
        }
        "traffic_signals" -> {
            if (map["crossing"] == "traffic_signals") return R.drawable.ic_pin_pedestrian_traffic_light
        }
    }
    when (map["leisure"]) {
        "picnic_table" -> return R.drawable.ic_pin_picnic_table
    }
    when (map["man_made"]) {
        "utility_pole" -> return R.drawable.ic_pin_power
        "water_well" -> return R.drawable.ic_pin_water
        "water_tap" -> return R.drawable.ic_pin_water
    }
    when (map["natural"]) {
        "spring" -> return R.drawable.ic_pin_water
    }
    when (map["power"]) {
        "pole" -> return R.drawable.ic_pin_power
    }
    if (map["surveillance"] != null && map["surveillance:type"] == "camera") {
        return R.drawable.ic_pin_surveillance_camera
    }
    when (map["tourism"]) {
        "information" -> return R.drawable.ic_pin_information
    }
    if (getHouseNumber(map) != null && getNameLabel(map) == null) {
        return R.drawable.ic_none
    }
    return null
}

fun getTitle(map: Map<String, String>): String? {
    return getNameLabel(map) ?: getHouseNumber(map)
}

private fun getHouseNumber(map: Map<String, String>): String? {
    val houseName = map["addr:housename"]
    val conscriptionNumber = map["addr:conscriptionnumber"]
    val streetNumber = map["addr:streetnumber"]
    val houseNumber = map["addr:housenumber"]

    return when {
        houseName != null -> houseName
        conscriptionNumber != null && streetNumber != null -> "$conscriptionNumber / $streetNumber"
        conscriptionNumber != null -> conscriptionNumber
        houseNumber != null -> houseNumber
        else -> null
    }
}
