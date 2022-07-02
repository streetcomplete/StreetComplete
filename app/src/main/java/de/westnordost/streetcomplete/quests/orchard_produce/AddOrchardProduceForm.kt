package de.westnordost.streetcomplete.quests.orchard_produce

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
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

class AddOrchardProduceForm : AImageListQuestForm<OrchardProduce, List<OrchardProduce>>() {

    private val produces = listOf(
        // ordered alphabetically here for overview. Produces are filtered and sorted by what is
        // found in the the country metadata

        Item(SISAL,         R.drawable.produce_sisal,       R.string.produce_sisal),
        Item(GRAPE,         R.drawable.produce_grape,       R.string.produce_grapes),

        Item(AGAVE,         R.drawable.produce_agave,       R.string.produce_agaves),
        Item(ALMOND,        R.drawable.produce_almond,      R.string.produce_almonds),
        Item(APPLE,         R.drawable.produce_apple,       R.string.produce_apples),
        Item(APRICOT,       R.drawable.produce_apricot,     R.string.produce_apricots),
        Item(ARECA_NUT,     R.drawable.produce_areca_nut,   R.string.produce_areca_nuts),
        Item(AVOCADO,       R.drawable.produce_avocado,     R.string.produce_avocados),
        Item(BANANA,        R.drawable.produce_banana,      R.string.produce_bananas),
        Item(SWEET_PEPPER,  R.drawable.produce_bell_pepper, R.string.produce_sweet_peppers),
        Item(BLUEBERRY,     R.drawable.produce_blueberry,   R.string.produce_blueberries),
        Item(BRAZIL_NUT,    R.drawable.produce_brazil_nut,  R.string.produce_brazil_nuts),
        Item(CACAO,         R.drawable.produce_cacao,       R.string.produce_cacao),
        Item(CASHEW,        R.drawable.produce_cashew,      R.string.produce_cashew_nuts),
        Item(CHERRY,        R.drawable.produce_cherry,      R.string.produce_cherries),
        Item(CHESTNUT,      R.drawable.produce_chestnut,    R.string.produce_chestnuts),
        Item(CHILLI_PEPPER, R.drawable.produce_chili,       R.string.produce_chili),
        Item(COCONUT,       R.drawable.produce_coconut,     R.string.produce_coconuts),
        Item(COFFEE,        R.drawable.produce_coffee,      R.string.produce_coffee),
        Item(CRANBERRY,     R.drawable.produce_cranberry,   R.string.produce_cranberries),
        Item(DATE,          R.drawable.produce_date,        R.string.produce_dates),
        Item(FIG,           R.drawable.produce_fig,         R.string.produce_figs),
        Item(GRAPEFRUIT,    R.drawable.produce_grapefruit,  R.string.produce_grapefruits),
        Item(GUAVA,         R.drawable.produce_guava,       R.string.produce_guavas),
        Item(HAZELNUT,      R.drawable.produce_hazelnut,    R.string.produce_hazelnuts),
        Item(HOP,           R.drawable.produce_hop,         R.string.produce_hops),
        Item(JOJOBA,        R.drawable.produce_jojoba,      R.string.produce_jojoba),
        Item(KIWI,          R.drawable.produce_kiwi,        R.string.produce_kiwis),
        Item(KOLA_NUT,      R.drawable.produce_kola_nut,    R.string.produce_kola_nuts),
        Item(LEMON,         R.drawable.produce_lemon,       R.string.produce_lemons),
        Item(LIME,          R.drawable.produce_lime,        R.string.produce_limes),
        Item(MANGO,         R.drawable.produce_mango,       R.string.produce_mangos),
        Item(MANGOSTEEN,    R.drawable.produce_mangosteen,  R.string.produce_mangosteen),
        Item(MATE,          R.drawable.produce_mate,        R.string.produce_mate),
        Item(NUTMEG,        R.drawable.produce_nutmeg,      R.string.produce_nutmeg),
        Item(OLIVE,         R.drawable.produce_olive,       R.string.produce_olives),
        Item(ORANGE,        R.drawable.produce_orange,      R.string.produce_oranges),
        Item(PALM_OIL,      R.drawable.produce_palm_oil,    R.string.produce_oil_palms),
        Item(PAPAYA,        R.drawable.produce_papaya,      R.string.produce_papayas),
        Item(PEACH,         R.drawable.produce_peach,       R.string.produce_peaches),
        Item(PEAR,          R.drawable.produce_pear,        R.string.produce_pears),
        Item(PEPPER,        R.drawable.produce_pepper,      R.string.produce_pepper),
        Item(PERSIMMON,     R.drawable.produce_persimmon,   R.string.produce_persimmons),
        Item(PINEAPPLE,     R.drawable.produce_pineapple,   R.string.produce_pineapples),
        Item(PISTACHIO,     R.drawable.produce_pistachio,   R.string.produce_pistachios),
        Item(PLUM,          R.drawable.produce_plum,        R.string.produce_plums),
        Item(RASPBERRY,     R.drawable.produce_raspberry,   R.string.produce_raspberries),
        Item(RUBBER,        R.drawable.produce_rubber,      R.string.produce_rubber),
        Item(STRAWBERRY,    R.drawable.produce_strawberry,  R.string.produce_strawberries),
        Item(TEA,           R.drawable.produce_tea,         R.string.produce_tea),
        Item(TOMATO,        R.drawable.produce_tomato,      R.string.produce_tomatoes),
        Item(TUNG_NUT,      R.drawable.produce_tung_nut,    R.string.produce_tung_nuts),
        Item(VANILLA,       R.drawable.produce_vanilla,     R.string.produce_vanilla),
        Item(WALNUT,        R.drawable.produce_walnut,      R.string.produce_walnuts)
    )
    private val producesMap = produces.associateBy { it.value!!.osmValue }

    // only include what is given for that country
    override val items get() = countryInfo.orchardProduces.mapNotNull { producesMap[it] }

    override val itemsPerRow = 3
    override val maxSelectableItems = -1

    override fun onClickOk(selectedItems: List<OrchardProduce>) {
        applyAnswer(selectedItems)
    }
}
