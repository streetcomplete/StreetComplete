package de.westnordost.streetcomplete.quests.orchard_produce

import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val OrchardProduce.title: StringResource get() = when (this) {
    SISAL ->         Res.string.produce_sisal
    GRAPE ->         Res.string.produce_grapes
    AGAVE ->         Res.string.produce_agaves
    ALMOND ->        Res.string.produce_almonds
    APPLE ->         Res.string.produce_apples
    APRICOT ->       Res.string.produce_apricots
    ARECA_NUT ->     Res.string.produce_areca_nuts
    AVOCADO ->       Res.string.produce_avocados
    BANANA ->        Res.string.produce_bananas
    SWEET_PEPPER ->  Res.string.produce_sweet_peppers
    BLUEBERRY ->     Res.string.produce_blueberries
    BRAZIL_NUT ->    Res.string.produce_brazil_nuts
    CACAO ->         Res.string.produce_cacao
    CASHEW ->        Res.string.produce_cashew_nuts
    CHERRY ->        Res.string.produce_cherries
    CHESTNUT ->      Res.string.produce_chestnuts
    CHILLI_PEPPER -> Res.string.produce_chili
    COCONUT ->       Res.string.produce_coconuts
    COFFEE ->        Res.string.produce_coffee
    CRANBERRY ->     Res.string.produce_cranberries
    DATE ->          Res.string.produce_dates
    FIG ->           Res.string.produce_figs
    GRAPEFRUIT ->    Res.string.produce_grapefruits
    GUAVA ->         Res.string.produce_guavas
    HAZELNUT ->      Res.string.produce_hazelnuts
    HOP ->           Res.string.produce_hops
    JOJOBA ->        Res.string.produce_jojoba
    KIWI ->          Res.string.produce_kiwis
    KOLA_NUT ->      Res.string.produce_kola_nuts
    LEMON ->         Res.string.produce_lemons
    LIME ->          Res.string.produce_limes
    MANGO ->         Res.string.produce_mangos
    MANGOSTEEN ->    Res.string.produce_mangosteen
    MATE ->          Res.string.produce_mate
    NUTMEG ->        Res.string.produce_nutmeg
    OLIVE ->         Res.string.produce_olives
    ORANGE ->        Res.string.produce_oranges
    PALM_OIL ->      Res.string.produce_oil_palms
    PAPAYA ->        Res.string.produce_papayas
    PEACH ->         Res.string.produce_peaches
    PEAR ->          Res.string.produce_pears
    PEPPER ->        Res.string.produce_pepper
    PERSIMMON ->     Res.string.produce_persimmons
    PINEAPPLE ->     Res.string.produce_pineapples
    PISTACHIO ->     Res.string.produce_pistachios
    PLUM ->          Res.string.produce_plums
    RASPBERRY ->     Res.string.produce_raspberries
    RUBBER ->        Res.string.produce_rubber
    STRAWBERRY ->    Res.string.produce_strawberries
    TEA ->           Res.string.produce_tea
    TOMATO ->        Res.string.produce_tomatoes
    TUNG_NUT ->      Res.string.produce_tung_nuts
    VANILLA ->       Res.string.produce_vanilla
    WALNUT ->        Res.string.produce_walnuts
}

val OrchardProduce.icon: DrawableResource get() = when (this) {
    SISAL ->         Res.drawable.produce_sisal
    GRAPE ->         Res.drawable.produce_grape
    AGAVE ->         Res.drawable.produce_agave
    ALMOND ->        Res.drawable.produce_almond
    APPLE ->         Res.drawable.produce_apple
    APRICOT ->       Res.drawable.produce_apricot
    ARECA_NUT ->     Res.drawable.produce_areca_nut
    AVOCADO ->       Res.drawable.produce_avocado
    BANANA ->        Res.drawable.produce_banana
    SWEET_PEPPER ->  Res.drawable.produce_bell_pepper
    BLUEBERRY ->     Res.drawable.produce_blueberry
    BRAZIL_NUT ->    Res.drawable.produce_brazil_nut
    CACAO ->         Res.drawable.produce_cacao
    CASHEW ->        Res.drawable.produce_cashew
    CHERRY ->        Res.drawable.produce_cherry
    CHESTNUT ->      Res.drawable.produce_chestnut
    CHILLI_PEPPER -> Res.drawable.produce_chili
    COCONUT ->       Res.drawable.produce_coconut
    COFFEE ->        Res.drawable.produce_coffee
    CRANBERRY ->     Res.drawable.produce_cranberry
    DATE ->          Res.drawable.produce_date
    FIG ->           Res.drawable.produce_fig
    GRAPEFRUIT ->    Res.drawable.produce_grapefruit
    GUAVA ->         Res.drawable.produce_guava
    HAZELNUT ->      Res.drawable.produce_hazelnut
    HOP ->           Res.drawable.produce_hop
    JOJOBA ->        Res.drawable.produce_jojoba
    KIWI ->          Res.drawable.produce_kiwi
    KOLA_NUT ->      Res.drawable.produce_kola_nut
    LEMON ->         Res.drawable.produce_lemon
    LIME ->          Res.drawable.produce_lime
    MANGO ->         Res.drawable.produce_mango
    MANGOSTEEN ->    Res.drawable.produce_mangosteen
    MATE ->          Res.drawable.produce_mate
    NUTMEG ->        Res.drawable.produce_nutmeg
    OLIVE ->         Res.drawable.produce_olive
    ORANGE ->        Res.drawable.produce_orange
    PALM_OIL ->      Res.drawable.produce_palm_oil
    PAPAYA ->        Res.drawable.produce_papaya
    PEACH ->         Res.drawable.produce_peach
    PEAR ->          Res.drawable.produce_pear
    PEPPER ->        Res.drawable.produce_pepper
    PERSIMMON ->     Res.drawable.produce_persimmon
    PINEAPPLE ->     Res.drawable.produce_pineapple
    PISTACHIO ->     Res.drawable.produce_pistachio
    PLUM ->          Res.drawable.produce_plum
    RASPBERRY ->     Res.drawable.produce_raspberry
    RUBBER ->        Res.drawable.produce_rubber
    STRAWBERRY ->    Res.drawable.produce_strawberry
    TEA ->           Res.drawable.produce_tea
    TOMATO ->        Res.drawable.produce_tomato
    TUNG_NUT ->      Res.drawable.produce_tung_nut
    VANILLA ->       Res.drawable.produce_vanilla
    WALNUT ->        Res.drawable.produce_walnut
}
