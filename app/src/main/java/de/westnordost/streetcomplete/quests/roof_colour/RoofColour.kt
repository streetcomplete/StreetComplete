package de.westnordost.streetcomplete.quests.roof_colour

import de.westnordost.streetcomplete.view.image_select.OsmColour

enum class RoofColour(override val osmValue: String, override val androidValue: String?) :
    OsmColour {
    // Top used roof colours
    DARK_GREY("darkgrey", "#a9a9a9"),
    GREY("grey", "#808080"),
    LIGHT_GREY("lightgrey", "#d3d3d3"),
    RED("red", "#ff0000"),
    BROWN("brown", "#a52a2a"),
    MAROON("maroon", "#800000"),
    BLACK("black", "#000000"),
    WHITE("white", "#ffffff"),
    SILVER("silver", "#c0c0c0"),
    BLUE("blue", "#0000ff"),
    SALMON("salmon", "#fa8072"),
    DESERT_SAND("#bbad8e", null),
    MOCHA("#938870", null),

    // Rest of the recommended 3D palette
    OLIVE("olive", "#808000"),
    GREEN("green", "#008000"),
    TEAL("teal", "#008080"),
    NAVY("navy", "#000080"),
    PURPLE("purple", "#800080"),
    YELLOW("yellow", "#ffff00"),
    LIME("lime", "#00ff00"),
    AQUA("aqua", "#00ffff"),
    FUCHSIA("fuchsia", "#ff00ff"),
}
