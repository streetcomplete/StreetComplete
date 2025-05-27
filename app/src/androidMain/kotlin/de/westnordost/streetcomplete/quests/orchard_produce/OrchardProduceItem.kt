package de.westnordost.streetcomplete.quests.orchard_produce

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.AGAVE
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.ALMOND
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.APPLE
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.APRICOT
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.ARECA_NUT
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.AVOCADO
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.BANANA
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.BLUEBERRY
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.BRAZIL_NUT
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.CACAO
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.CASHEW
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.CHERRY
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.CHESTNUT
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.CHILLI_PEPPER
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.COCONUT
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.COFFEE
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.CRANBERRY
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.DATE
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.FIG
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.GRAPE
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.GRAPEFRUIT
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.GUAVA
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.HAZELNUT
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.HOP
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.JOJOBA
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.KIWI
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.KOLA_NUT
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.LEMON
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.LIME
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.MANGO
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.MANGOSTEEN
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.MATE
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.NUTMEG
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.OLIVE
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.ORANGE
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.PALM_OIL
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.PAPAYA
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.PEACH
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.PEAR
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.PEPPER
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.PERSIMMON
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.PINEAPPLE
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.PISTACHIO
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.PLUM
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.RASPBERRY
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.RUBBER
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.SISAL
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.STRAWBERRY
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.SWEET_PEPPER
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.TEA
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.TOMATO
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.TUNG_NUT
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.VANILLA
import de.westnordost.streetcomplete.quests.orchard_produce.OrchardProduce.WALNUT
import de.westnordost.streetcomplete.view.image_select.Item

fun OrchardProduce.asItem() = Item(this, iconResId, titleResId)

private val OrchardProduce.titleResId: Int get() = when (this) {
    SISAL ->         R.string.produce_sisal
    GRAPE ->         R.string.produce_grapes
    AGAVE ->         R.string.produce_agaves
    ALMOND ->        R.string.produce_almonds
    APPLE ->         R.string.produce_apples
    APRICOT ->       R.string.produce_apricots
    ARECA_NUT ->     R.string.produce_areca_nuts
    AVOCADO ->       R.string.produce_avocados
    BANANA ->        R.string.produce_bananas
    SWEET_PEPPER ->  R.string.produce_sweet_peppers
    BLUEBERRY ->     R.string.produce_blueberries
    BRAZIL_NUT ->    R.string.produce_brazil_nuts
    CACAO ->         R.string.produce_cacao
    CASHEW ->        R.string.produce_cashew_nuts
    CHERRY ->        R.string.produce_cherries
    CHESTNUT ->      R.string.produce_chestnuts
    CHILLI_PEPPER -> R.string.produce_chili
    COCONUT ->       R.string.produce_coconuts
    COFFEE ->        R.string.produce_coffee
    CRANBERRY ->     R.string.produce_cranberries
    DATE ->          R.string.produce_dates
    FIG ->           R.string.produce_figs
    GRAPEFRUIT ->    R.string.produce_grapefruits
    GUAVA ->         R.string.produce_guavas
    HAZELNUT ->      R.string.produce_hazelnuts
    HOP ->           R.string.produce_hops
    JOJOBA ->        R.string.produce_jojoba
    KIWI ->          R.string.produce_kiwis
    KOLA_NUT ->      R.string.produce_kola_nuts
    LEMON ->         R.string.produce_lemons
    LIME ->          R.string.produce_limes
    MANGO ->         R.string.produce_mangos
    MANGOSTEEN ->    R.string.produce_mangosteen
    MATE ->          R.string.produce_mate
    NUTMEG ->        R.string.produce_nutmeg
    OLIVE ->         R.string.produce_olives
    ORANGE ->        R.string.produce_oranges
    PALM_OIL ->      R.string.produce_oil_palms
    PAPAYA ->        R.string.produce_papayas
    PEACH ->         R.string.produce_peaches
    PEAR ->          R.string.produce_pears
    PEPPER ->        R.string.produce_pepper
    PERSIMMON ->     R.string.produce_persimmons
    PINEAPPLE ->     R.string.produce_pineapples
    PISTACHIO ->     R.string.produce_pistachios
    PLUM ->          R.string.produce_plums
    RASPBERRY ->     R.string.produce_raspberries
    RUBBER ->        R.string.produce_rubber
    STRAWBERRY ->    R.string.produce_strawberries
    TEA ->           R.string.produce_tea
    TOMATO ->        R.string.produce_tomatoes
    TUNG_NUT ->      R.string.produce_tung_nuts
    VANILLA ->       R.string.produce_vanilla
    WALNUT ->        R.string.produce_walnuts
}

private val OrchardProduce.iconResId: Int get() = when (this) {
    SISAL ->         R.drawable.produce_sisal
    GRAPE ->         R.drawable.produce_grape
    AGAVE ->         R.drawable.produce_agave
    ALMOND ->        R.drawable.produce_almond
    APPLE ->         R.drawable.produce_apple
    APRICOT ->       R.drawable.produce_apricot
    ARECA_NUT ->     R.drawable.produce_areca_nut
    AVOCADO ->       R.drawable.produce_avocado
    BANANA ->        R.drawable.produce_banana
    SWEET_PEPPER ->  R.drawable.produce_bell_pepper
    BLUEBERRY ->     R.drawable.produce_blueberry
    BRAZIL_NUT ->    R.drawable.produce_brazil_nut
    CACAO ->         R.drawable.produce_cacao
    CASHEW ->        R.drawable.produce_cashew
    CHERRY ->        R.drawable.produce_cherry
    CHESTNUT ->      R.drawable.produce_chestnut
    CHILLI_PEPPER -> R.drawable.produce_chili
    COCONUT ->       R.drawable.produce_coconut
    COFFEE ->        R.drawable.produce_coffee
    CRANBERRY ->     R.drawable.produce_cranberry
    DATE ->          R.drawable.produce_date
    FIG ->           R.drawable.produce_fig
    GRAPEFRUIT ->    R.drawable.produce_grapefruit
    GUAVA ->         R.drawable.produce_guava
    HAZELNUT ->      R.drawable.produce_hazelnut
    HOP ->           R.drawable.produce_hop
    JOJOBA ->        R.drawable.produce_jojoba
    KIWI ->          R.drawable.produce_kiwi
    KOLA_NUT ->      R.drawable.produce_kola_nut
    LEMON ->         R.drawable.produce_lemon
    LIME ->          R.drawable.produce_lime
    MANGO ->         R.drawable.produce_mango
    MANGOSTEEN ->    R.drawable.produce_mangosteen
    MATE ->          R.drawable.produce_mate
    NUTMEG ->        R.drawable.produce_nutmeg
    OLIVE ->         R.drawable.produce_olive
    ORANGE ->        R.drawable.produce_orange
    PALM_OIL ->      R.drawable.produce_palm_oil
    PAPAYA ->        R.drawable.produce_papaya
    PEACH ->         R.drawable.produce_peach
    PEAR ->          R.drawable.produce_pear
    PEPPER ->        R.drawable.produce_pepper
    PERSIMMON ->     R.drawable.produce_persimmon
    PINEAPPLE ->     R.drawable.produce_pineapple
    PISTACHIO ->     R.drawable.produce_pistachio
    PLUM ->          R.drawable.produce_plum
    RASPBERRY ->     R.drawable.produce_raspberry
    RUBBER ->        R.drawable.produce_rubber
    STRAWBERRY ->    R.drawable.produce_strawberry
    TEA ->           R.drawable.produce_tea
    TOMATO ->        R.drawable.produce_tomato
    TUNG_NUT ->      R.drawable.produce_tung_nut
    VANILLA ->       R.drawable.produce_vanilla
    WALNUT ->        R.drawable.produce_walnut
}
