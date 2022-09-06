package de.westnordost.streetcomplete.quests.recycling_material

/** All recycling:* keys known to StreetComplete */
enum class RecyclingMaterial(val value: String) {
    GLASS_BOTTLES("glass_bottles"),
    GLASS("glass"),
    PAPER("paper"),
    PLASTIC("plastic"),
    PLASTIC_PACKAGING("plastic_packaging"),
    PLASTIC_BOTTLES("plastic_bottles"),
    PET("PET"),
    BEVERAGE_CARTONS("beverage_cartons"),
    CANS("cans"),
    SCRAP_METAL("scrap_metal"),
    CLOTHES("clothes"),
    SHOES("shoes"),
    SMALL_ELECTRICAL_APPLIANCES("small_electrical_appliances"),
    BATTERIES("batteries"),
    GREEN_WASTE("green_waste"),
    COOKING_OIL("cooking_oil"),
    ENGINE_OIL("engine_oil");

    companion object {
        val selectableValues = listOf(
            GLASS_BOTTLES, PAPER, PLASTIC, CANS, SCRAP_METAL, CLOTHES, SHOES,
            SMALL_ELECTRICAL_APPLIANCES, BATTERIES, GREEN_WASTE, COOKING_OIL, ENGINE_OIL
        )

        val selectablePlasticValues = listOf(
            listOf(PLASTIC),
            listOf(PLASTIC_PACKAGING),
            listOf(PLASTIC_BOTTLES, BEVERAGE_CARTONS),
            listOf(PLASTIC_BOTTLES),
            listOf(PET),
            listOf(BEVERAGE_CARTONS)
        )

        val selectableGlassValues = listOf(
            listOf(GLASS_BOTTLES),
            listOf(GLASS)
        )

        val plastics = setOf(PLASTIC, PLASTIC_PACKAGING, PLASTIC_BOTTLES, BEVERAGE_CARTONS, PET)
    }
}
