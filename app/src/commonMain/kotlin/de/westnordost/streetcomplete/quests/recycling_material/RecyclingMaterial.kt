package de.westnordost.streetcomplete.quests.recycling_material

import de.westnordost.streetcomplete.util.tree.buildTree

/** All recycling:* keys known to StreetComplete */
enum class RecyclingMaterial(val value: String) {
    // sorted how they should appear in the UI, roughly by how common they are

    // most common (paper, glass, plastic packaging)
    GLASS_BOTTLES("glass_bottles"),
    PAPER("paper"),
    PLASTIC_PACKAGING("plastic_packaging"),
    CANS("cans"),

    // sometimes additionally accepted at normal recycling containers (scrap metal, any plastic, any glass)
    // or sometimes less than usual accepted at recycling containers
    BEVERAGE_CARTONS("beverage_cartons"),
    PLASTIC_BOTTLES("plastic_bottles"),
    PET_BOTTLES("pet_drink_bottles"),
    PLASTIC("plastic"),
    GLASS("glass"),
    SCRAP_METAL("scrap_metal"),

    // specific containers but very common
    CLOTHES("clothes"),
    SHOES("shoes"),

    // specific stuff usually not present at normal recycling containers
    SMALL_ELECTRICAL_APPLIANCES("small_electrical_appliances"),
    BATTERIES("batteries"),
    GREEN_WASTE("green_waste"),
    FOOD_WASTE("food_waste"),
    COOKING_OIL("cooking_oil"),
    ENGINE_OIL("engine_oil");

    companion object {
        val tree by lazy { buildTree<RecyclingMaterial> {
            nd(GLASS) {
                nd(GLASS_BOTTLES)
            }
            nd(PAPER)
            nd(PLASTIC) {
                nd(PLASTIC_PACKAGING) {
                    nd(BEVERAGE_CARTONS)
                    nd(PLASTIC_BOTTLES) {
                        nd(PET_BOTTLES)
                    }
                }
            }
            nd(CANS)
            nd(SCRAP_METAL)
            nd(CLOTHES)
            nd(SHOES)
            nd(SMALL_ELECTRICAL_APPLIANCES)
            nd(BATTERIES)
            nd(GREEN_WASTE)
            nd(FOOD_WASTE)
            nd(COOKING_OIL)
            nd(ENGINE_OIL)
        } }
    }
}
