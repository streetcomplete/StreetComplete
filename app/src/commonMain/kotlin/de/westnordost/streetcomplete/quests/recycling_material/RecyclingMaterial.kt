package de.westnordost.streetcomplete.quests.recycling_material

import de.westnordost.streetcomplete.util.tree.buildTree

/** All recycling:* keys known to StreetComplete */
enum class RecyclingMaterial(val value: String) {
    GLASS_BOTTLES("glass_bottles"),
    GLASS("glass"),
    PAPER("paper"),
    PLASTIC("plastic"),
    PLASTIC_PACKAGING("plastic_packaging"),
    PLASTIC_BOTTLES("plastic_bottles"),
    PET_BOTTLES("pet_drink_bottles"),
    BEVERAGE_CARTONS("beverage_cartons"),
    CANS("cans"),
    SCRAP_METAL("scrap_metal"),
    CLOTHES("clothes"),
    SHOES("shoes"),
    SMALL_ELECTRICAL_APPLIANCES("small_electrical_appliances"),
    BATTERIES("batteries"),
    GREEN_WASTE("green_waste"),
    FOOD_WASTE("food_waste"),
    COOKING_OIL("cooking_oil"),
    ENGINE_OIL("engine_oil");

    companion object {
        val tree = buildTree<RecyclingMaterial> {
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
        }
    }
}
