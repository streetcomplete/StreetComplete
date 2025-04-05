package de.westnordost.streetcomplete.quests.building_material

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

enum class BuildingMaterial(
    val osmValue: String,
    @DrawableRes val imageResId: Int,
    @StringRes val titleResId: Int,
) {
    CEMENT_BLOCK(
        osmValue = "cement_block",
        imageResId = R.drawable.building_material_cement_block,
        titleResId = R.string.quest_material_cement_block
    ),
    BRICK(
        osmValue = "brick",
        imageResId = R.drawable.building_material_brick,
        titleResId = R.string.quest_material_brick
    ),
    PLASTER(
        osmValue = "plaster",
        imageResId = R.drawable.building_material_plaster,
        titleResId = R.string.quest_material_plaster
    ),
    WOOD(
        osmValue = "wood",
        imageResId = R.drawable.building_material_wood,
        titleResId = R.string.quest_material_wood
    ),
    CONCRETE(
        osmValue = "concrete",
        imageResId = R.drawable.building_material_concrete,
        titleResId = R.string.quest_material_concrete
    ),
    METAL(
        osmValue = "metal",
        imageResId = R.drawable.building_material_metal,
        titleResId = R.string.quest_material_metal
    ),
    STONE(
        osmValue = "stone",
        imageResId = R.drawable.building_material_stone,
        titleResId = R.string.quest_material_stone
    ),
    GLASS(
        osmValue = "glass",
        imageResId = R.drawable.building_material_glass,
        titleResId = R.string.quest_material_glass
    ),
    MIRROR(
        osmValue = "mirror",
        imageResId = R.drawable.building_material_mirror,
        titleResId = R.string.quest_material_mirror
    ),
    MUD(
        osmValue = "mud",
        imageResId = R.drawable.building_material_mud,
        titleResId = R.string.quest_material_mud
    ),
    PLASTIC(
        osmValue = "plastic",
        imageResId = R.drawable.building_material_plastic,
        titleResId = R.string.quest_material_plastic
    ),
    TIMBER_FRAMING(
        osmValue = "timber_framing",
        imageResId = R.drawable.building_material_timber_framing,
        titleResId = R.string.quest_material_timber_framing
    ),
    SANDSTONE(
        osmValue = "sandstone",
        imageResId = R.drawable.building_material_sandstone,
        titleResId = R.string.quest_material_sandstone
    ),
    CLAY(
        osmValue = "clay",
        imageResId = R.drawable.building_material_clay,
        titleResId = R.string.quest_material_clay
    ),
    REED(
        osmValue = "reed",
        imageResId = R.drawable.building_material_reed,
        titleResId = R.string.quest_material_reed
    ),
    LOAM(
        osmValue = "loam",
        imageResId = R.drawable.building_material_loam,
        titleResId = R.string.quest_material_loam
    ),
    MARBLE(
        osmValue = "marble",
        imageResId = R.drawable.building_material_marble,
        titleResId = R.string.quest_material_marble
    ),
    SLATE(
        osmValue = "slate",
        imageResId = R.drawable.building_material_slate,
        titleResId = R.string.quest_material_slate
    ),
    VINYL(
        osmValue = "vinyl",
        imageResId = R.drawable.building_material_vinyl,
        titleResId = R.string.quest_material_vinyl
    ),
    LIMESTONE(
        osmValue = "limestone",
        imageResId = R.drawable.building_material_limestone,
        titleResId = R.string.quest_material_limestone
    ),
    TILES(
        osmValue = "tiles",
        imageResId = R.drawable.building_material_tiles,
        titleResId = R.string.quest_material_tiles
    ),
    BAMBOO(
        osmValue = "bamboo",
        imageResId = R.drawable.building_material_bamboo,
        titleResId = R.string.quest_material_bamboo
    ),
    ADOBE(
        osmValue = "adobe",
        imageResId = R.drawable.building_material_adobe,
        titleResId = R.string.quest_material_adobe
    )
}

fun Collection<BuildingMaterial>.toItems() = map { it.asItem() }

fun BuildingMaterial.asItem(): DisplayItem<BuildingMaterial> = Item(this, imageResId, titleResId)
