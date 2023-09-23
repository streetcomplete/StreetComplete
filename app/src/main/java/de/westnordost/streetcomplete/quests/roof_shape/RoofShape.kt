package de.westnordost.streetcomplete.quests.roof_shape

enum class RoofShape(val osmValue: String) {
    GABLED("gabled"),
    HIPPED("hipped"),
    FLAT("flat"),
    PYRAMIDAL("pyramidal"),

    HALF_HIPPED("half-hipped"),
    SKILLION("skillion"),
    GAMBREL("gambrel"),
    ROUND("round"),

    DOUBLE_SALTBOX("double_saltbox"),
    SALTBOX("saltbox"),
    MANSARD("mansard"),
    DOME("dome"),

    QUADRUPLE_SALTBOX("quadruple_saltbox"),
    ROUND_GABLED("round_gabled"),
    ONION("onion"),
    CONE("cone"),

    SAWTOOTH("sawtooth"),
    HIPPED_AND_GABLED("hipped-and-gabled"),
    CROSSPITCHED("crosspitched"),
    SIDE_HIPPED("side_hipped"),
    SIDE_HALF_HIPPED("side_half-hipped"),
    GABLED_HEIGHT_MOVED("gabled_height_moved"),

    MANY("many"),
}
