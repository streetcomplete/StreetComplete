package de.westnordost.streetcomplete.quests.orchard_produce

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class AddOrchardProduceForm : AImageListQuestAnswerFragment<String, List<String>>() {

    private val produces = listOf(
        // ordered alphabetically here for overview. Produces are filtered and sorted by what is
        // found in the the country metadata

        // may have been mistaken for an orchard (i.e. agave) from satellite imagery
        // landuse=farmland
        Item("sisal",         R.drawable.produce_sisal,       R.string.produce_sisal),
        // landuse=vineyard
        Item("grape",         R.drawable.produce_grape,       R.string.produce_grapes),

        Item("agave",         R.drawable.produce_agave,       R.string.produce_agaves),
        Item("almond",        R.drawable.produce_almond,      R.string.produce_almonds),
        Item("apple",         R.drawable.produce_apple,       R.string.produce_apples),
        Item("apricot",       R.drawable.produce_apricot,     R.string.produce_apricots),
        Item("areca_nut",     R.drawable.produce_areca_nut,   R.string.produce_areca_nuts),
        Item("avocado",       R.drawable.produce_avocado,     R.string.produce_avocados),
        Item("banana",        R.drawable.produce_banana,      R.string.produce_bananas),
        Item("sweet_pepper",  R.drawable.produce_bell_pepper, R.string.produce_sweet_peppers),
        Item("blueberry",     R.drawable.produce_blueberry,   R.string.produce_blueberries),
        Item("brazil_nut",    R.drawable.produce_brazil_nut,  R.string.produce_brazil_nuts),
        Item("cacao",         R.drawable.produce_cacao,       R.string.produce_cacao),
        Item("cashew",        R.drawable.produce_cashew,      R.string.produce_cashew_nuts),
        Item("cherry",        R.drawable.produce_cherry,      R.string.produce_cherries),
        Item("chestnut",      R.drawable.produce_chestnut,    R.string.produce_chestnuts),
        Item("chilli_pepper", R.drawable.produce_chili,       R.string.produce_chili),
        Item("coconut",       R.drawable.produce_coconut,     R.string.produce_coconuts),
        Item("coffee",        R.drawable.produce_coffee,      R.string.produce_coffee),
        Item("cranberry",     R.drawable.produce_cranberry,   R.string.produce_cranberries),
        Item("date",          R.drawable.produce_date,        R.string.produce_dates),
        Item("fig",           R.drawable.produce_fig,         R.string.produce_figs),
        Item("grapefruit",    R.drawable.produce_grapefruit,  R.string.produce_grapefruits),
        Item("guava",         R.drawable.produce_guava,       R.string.produce_guavas),
        Item("hazelnut",      R.drawable.produce_hazelnut,    R.string.produce_hazelnuts),
        Item("hop",           R.drawable.produce_hop,         R.string.produce_hops),
        Item("jojoba",        R.drawable.produce_jojoba,      R.string.produce_jojoba),
        Item("kiwi",          R.drawable.produce_kiwi,        R.string.produce_kiwis),
        Item("kola_nut",      R.drawable.produce_kola_nut,    R.string.produce_kola_nuts),
        Item("lemon",         R.drawable.produce_lemon,       R.string.produce_lemons),
        Item("lime",          R.drawable.produce_lime,        R.string.produce_limes),
        Item("mango",         R.drawable.produce_mango,       R.string.produce_mangos),
        Item("mangosteen",    R.drawable.produce_mangosteen,  R.string.produce_mangosteen),
        Item("mate",          R.drawable.produce_mate,        R.string.produce_mate),
        Item("nutmeg",        R.drawable.produce_nutmeg,      R.string.produce_nutmeg),
        Item("olive",         R.drawable.produce_olive,       R.string.produce_olives),
        Item("orange",        R.drawable.produce_orange,      R.string.produce_oranges),
        Item("palm_oil",      R.drawable.produce_palm_oil,    R.string.produce_oil_palms),
        Item("papaya",        R.drawable.produce_papaya,      R.string.produce_papayas),
        Item("peach",         R.drawable.produce_peach,       R.string.produce_peaches),
        Item("pear",          R.drawable.produce_pear,        R.string.produce_pears),
        Item("pepper",        R.drawable.produce_pepper,      R.string.produce_pepper),
        Item("persimmon",     R.drawable.produce_persimmon,   R.string.produce_persimmons),
        Item("pineapple",     R.drawable.produce_pineapple,   R.string.produce_pineapples),
        Item("pistachio",     R.drawable.produce_pistachio,   R.string.produce_pistachios),
        Item("plum",          R.drawable.produce_plum,        R.string.produce_plums),
        Item("raspberry",     R.drawable.produce_raspberry,   R.string.produce_raspberries),
        Item("rubber",        R.drawable.produce_rubber,      R.string.produce_rubber),
        Item("strawberry",    R.drawable.produce_strawberry,  R.string.produce_strawberries),
        Item("tea",           R.drawable.produce_tea,         R.string.produce_tea),
        Item("tomato",        R.drawable.produce_tomato,      R.string.produce_tomatoes),
        Item("tung_nut",      R.drawable.produce_tung_nut,    R.string.produce_tung_nuts),
        Item("vanilla",       R.drawable.produce_vanilla,     R.string.produce_vanilla),
        Item("walnut",        R.drawable.produce_walnut,      R.string.produce_walnuts)
    )
    private val producesMap = produces.associateBy { it.value }

    // only include what is given for that country
    override val items get() = countryInfo.orchardProduces.mapNotNull { producesMap[it] }

    override val itemsPerRow = 3
    override val maxNumberOfInitiallyShownItems = -1
    override val maxSelectableItems = -1

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems)
    }
}
