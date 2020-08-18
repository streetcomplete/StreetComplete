package de.westnordost.streetcomplete.quests.recycling_material

/** All known recycling:* keys known to StreetComplete */
enum class RecyclingMaterial(val value: String) {
    GLASS_BOTTLES("glass_bottles"),
    PAPER("paper"),
    PLASTIC("plastic"),
    PLASTIC_PACKAGING("plastic_packaging"),
    PLASTIC_BOTTLES("plastic_bottles"),
    CANS("cans"),
    SCRAP_METAL("scrap_metal"),
    CLOTHES("clothes"),
    SHOES("shoes"),
    SMALL_ELECTRICAL_APPLIANCES("small_electrical_appliances"),
    BATTERIES("batteries"),
    GREEN_WASTE("green_waste"),
    COOKING_OIL("cooking_oil"),
    ENGINE_OIL("engine_oil")
}