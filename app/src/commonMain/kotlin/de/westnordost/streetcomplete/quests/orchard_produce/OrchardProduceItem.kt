package de.westnordost.streetcomplete.quests.orchard_produce

import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.produce_agave
import de.westnordost.streetcomplete.resources.produce_agaves
import de.westnordost.streetcomplete.resources.produce_almond
import de.westnordost.streetcomplete.resources.produce_almonds
import de.westnordost.streetcomplete.resources.produce_apple
import de.westnordost.streetcomplete.resources.produce_apples
import de.westnordost.streetcomplete.resources.produce_apricot
import de.westnordost.streetcomplete.resources.produce_apricots
import de.westnordost.streetcomplete.resources.produce_areca_nut
import de.westnordost.streetcomplete.resources.produce_areca_nuts
import de.westnordost.streetcomplete.resources.produce_avocado
import de.westnordost.streetcomplete.resources.produce_avocados
import de.westnordost.streetcomplete.resources.produce_banana
import de.westnordost.streetcomplete.resources.produce_bananas
import de.westnordost.streetcomplete.resources.produce_bell_pepper
import de.westnordost.streetcomplete.resources.produce_blueberries
import de.westnordost.streetcomplete.resources.produce_blueberry
import de.westnordost.streetcomplete.resources.produce_brazil_nut
import de.westnordost.streetcomplete.resources.produce_brazil_nuts
import de.westnordost.streetcomplete.resources.produce_cacao
import de.westnordost.streetcomplete.resources.produce_cashew
import de.westnordost.streetcomplete.resources.produce_cashew_nuts
import de.westnordost.streetcomplete.resources.produce_cherries
import de.westnordost.streetcomplete.resources.produce_cherry
import de.westnordost.streetcomplete.resources.produce_chestnut
import de.westnordost.streetcomplete.resources.produce_chestnuts
import de.westnordost.streetcomplete.resources.produce_chili
import de.westnordost.streetcomplete.resources.produce_coconut
import de.westnordost.streetcomplete.resources.produce_coconuts
import de.westnordost.streetcomplete.resources.produce_coffee
import de.westnordost.streetcomplete.resources.produce_cranberries
import de.westnordost.streetcomplete.resources.produce_cranberry
import de.westnordost.streetcomplete.resources.produce_date
import de.westnordost.streetcomplete.resources.produce_dates
import de.westnordost.streetcomplete.resources.produce_fig
import de.westnordost.streetcomplete.resources.produce_figs
import de.westnordost.streetcomplete.resources.produce_grape
import de.westnordost.streetcomplete.resources.produce_grapefruit
import de.westnordost.streetcomplete.resources.produce_grapefruits
import de.westnordost.streetcomplete.resources.produce_grapes
import de.westnordost.streetcomplete.resources.produce_guava
import de.westnordost.streetcomplete.resources.produce_guavas
import de.westnordost.streetcomplete.resources.produce_hazelnut
import de.westnordost.streetcomplete.resources.produce_hazelnuts
import de.westnordost.streetcomplete.resources.produce_hop
import de.westnordost.streetcomplete.resources.produce_hops
import de.westnordost.streetcomplete.resources.produce_jojoba
import de.westnordost.streetcomplete.resources.produce_kiwi
import de.westnordost.streetcomplete.resources.produce_kiwis
import de.westnordost.streetcomplete.resources.produce_kola_nut
import de.westnordost.streetcomplete.resources.produce_kola_nuts
import de.westnordost.streetcomplete.resources.produce_lemon
import de.westnordost.streetcomplete.resources.produce_lemons
import de.westnordost.streetcomplete.resources.produce_lime
import de.westnordost.streetcomplete.resources.produce_limes
import de.westnordost.streetcomplete.resources.produce_mango
import de.westnordost.streetcomplete.resources.produce_mangos
import de.westnordost.streetcomplete.resources.produce_mangosteen
import de.westnordost.streetcomplete.resources.produce_mate
import de.westnordost.streetcomplete.resources.produce_nutmeg
import de.westnordost.streetcomplete.resources.produce_oil_palms
import de.westnordost.streetcomplete.resources.produce_olive
import de.westnordost.streetcomplete.resources.produce_olives
import de.westnordost.streetcomplete.resources.produce_orange
import de.westnordost.streetcomplete.resources.produce_oranges
import de.westnordost.streetcomplete.resources.produce_palm_oil
import de.westnordost.streetcomplete.resources.produce_papaya
import de.westnordost.streetcomplete.resources.produce_papayas
import de.westnordost.streetcomplete.resources.produce_peach
import de.westnordost.streetcomplete.resources.produce_peaches
import de.westnordost.streetcomplete.resources.produce_pear
import de.westnordost.streetcomplete.resources.produce_pears
import de.westnordost.streetcomplete.resources.produce_pepper
import de.westnordost.streetcomplete.resources.produce_persimmon
import de.westnordost.streetcomplete.resources.produce_persimmons
import de.westnordost.streetcomplete.resources.produce_pineapple
import de.westnordost.streetcomplete.resources.produce_pineapples
import de.westnordost.streetcomplete.resources.produce_pistachio
import de.westnordost.streetcomplete.resources.produce_pistachios
import de.westnordost.streetcomplete.resources.produce_plum
import de.westnordost.streetcomplete.resources.produce_plums
import de.westnordost.streetcomplete.resources.produce_raspberries
import de.westnordost.streetcomplete.resources.produce_raspberry
import de.westnordost.streetcomplete.resources.produce_rubber
import de.westnordost.streetcomplete.resources.produce_sisal
import de.westnordost.streetcomplete.resources.produce_strawberries
import de.westnordost.streetcomplete.resources.produce_strawberry
import de.westnordost.streetcomplete.resources.produce_sweet_peppers
import de.westnordost.streetcomplete.resources.produce_tea
import de.westnordost.streetcomplete.resources.produce_tomato
import de.westnordost.streetcomplete.resources.produce_tomatoes
import de.westnordost.streetcomplete.resources.produce_tung_nut
import de.westnordost.streetcomplete.resources.produce_tung_nuts
import de.westnordost.streetcomplete.resources.produce_vanilla
import de.westnordost.streetcomplete.resources.produce_walnut
import de.westnordost.streetcomplete.resources.produce_walnuts
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
