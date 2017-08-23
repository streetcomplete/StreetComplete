package de.westnordost.streetcomplete.quests.orchard_produce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;

public class AddOrchardProduceForm extends ImageListQuestAnswerFragment
{
	private static final OsmItem[] PRODUCES = new OsmItem[]{
			// ordered alphabetically here for overview

			// may have been mistaken for an orchard (i.e. agave) from satellite imagery
			// landuse=farmland
			new OsmItem("sisal",		R.drawable.produce_sisal, R.string.produce_sisal),
			// landuse=vineyard
			new OsmItem("grape",		R.drawable.produce_grape, R.string.produce_grapes),

			new OsmItem("agave",		R.drawable.produce_agave, R.string.produce_agaves),
			new OsmItem("almond",		R.drawable.produce_almond, R.string.produce_almonds),
			new OsmItem("apple",		R.drawable.produce_apple, R.string.produce_apples),
			new OsmItem("apricot",		R.drawable.produce_apricot, R.string.produce_apricots),
			new OsmItem("areca_nut",	R.drawable.produce_areca_nut, R.string.produce_areca_nuts),
			new OsmItem("avocado",		R.drawable.produce_avocado, R.string.produce_avocados),
			new OsmItem("banana",		R.drawable.produce_banana, R.string.produce_bananas),
			new OsmItem("sweet_pepper",	R.drawable.produce_bell_pepper, R.string.produce_sweet_peppers),
			new OsmItem("blueberry",	R.drawable.produce_blueberry, R.string.produce_blueberries),
			new OsmItem("brazil_nut",	R.drawable.produce_brazil_nut, R.string.produce_brazil_nuts),
			new OsmItem("cacao",		R.drawable.produce_cacao, R.string.produce_cacao),
			new OsmItem("cashew",		R.drawable.produce_cashew, R.string.produce_cashew_nuts),
			new OsmItem("cherry",		R.drawable.produce_cherry, R.string.produce_cherries),
			new OsmItem("chestnut",		R.drawable.produce_chestnut, R.string.produce_chestnuts),
			new OsmItem("chilli_pepper",R.drawable.produce_chili, R.string.produce_chili),
			new OsmItem("coconut",		R.drawable.produce_coconut, R.string.produce_coconuts),
			new OsmItem("coffee",		R.drawable.produce_coffee, R.string.produce_coffee),
			new OsmItem("cranberry",	R.drawable.produce_cranberry, R.string.produce_cranberries),
			new OsmItem("date",			R.drawable.produce_date, R.string.produce_dates),
			new OsmItem("fig",			R.drawable.produce_fig, R.string.produce_figs),
			new OsmItem("grapefruit",	R.drawable.produce_grapefruit, R.string.produce_grapefruits),
			new OsmItem("guava",		R.drawable.produce_guava, R.string.produce_guavas),
			new OsmItem("hazelnut",		R.drawable.produce_hazelnut, R.string.produce_hazelnuts),
			new OsmItem("hop",			R.drawable.produce_hop, R.string.produce_hops),
			new OsmItem("jojoba",		R.drawable.produce_jojoba, R.string.produce_jojoba),
			new OsmItem("kiwi",			R.drawable.produce_kiwi, R.string.produce_kiwis),
			new OsmItem("kola_nut",		R.drawable.produce_kola_nut, R.string.produce_kola_nuts),
			new OsmItem("lemon",		R.drawable.produce_lemon, R.string.produce_lemons),
			new OsmItem("lime",			R.drawable.produce_lime, R.string.produce_limes),
			new OsmItem("mango",		R.drawable.produce_mango, R.string.produce_mangos),
			new OsmItem("mangosteen",	R.drawable.produce_mangosteen, R.string.produce_mangosteen),
			new OsmItem("mate",			R.drawable.produce_mate, R.string.produce_mate),
			new OsmItem("nutmeg",		R.drawable.produce_nutmeg, R.string.produce_nutmeg),
			new OsmItem("olive",		R.drawable.produce_olive, R.string.produce_olives),
			new OsmItem("orange",		R.drawable.produce_orange, R.string.produce_oranges),
			new OsmItem("palm_oil",		R.drawable.produce_palm_oil, R.string.produce_oil_palms),
			new OsmItem("papaya",		R.drawable.produce_papaya, R.string.produce_papayas),
			new OsmItem("peach",		R.drawable.produce_peach, R.string.produce_peaches),
			new OsmItem("pear",			R.drawable.produce_pear, R.string.produce_pears),
			new OsmItem("pepper",		R.drawable.produce_pepper, R.string.produce_pepper),
			new OsmItem("persimmon",	R.drawable.produce_persimmon, R.string.produce_persimmons),
			new OsmItem("pineapple", 	R.drawable.produce_pineapple, R.string.produce_pineapples),
			new OsmItem("pistachio",	R.drawable.produce_pistachio, R.string.produce_pistachios),
			new OsmItem("plum",			R.drawable.produce_plum, R.string.produce_plums),
			new OsmItem("raspberry",	R.drawable.produce_raspberry, R.string.produce_raspberries),
			new OsmItem("rubber",		R.drawable.produce_rubber, R.string.produce_rubber),
			new OsmItem("strawberry",	R.drawable.produce_strawberry, R.string.produce_strawberries),
			new OsmItem("tea",			R.drawable.produce_tea, R.string.produce_tea),
			new OsmItem("tomatoe",		R.drawable.produce_tomatoe, R.string.produce_tomatoes),
			new OsmItem("tung_nut",		R.drawable.produce_tung_nut, R.string.produce_tung_nuts),
			new OsmItem("vanilla",		R.drawable.produce_vanilla, R.string.produce_vanilla),
			new OsmItem("walnut",		R.drawable.produce_walnut, R.string.produce_walnuts),
	};
	private static final Map<String,OsmItem> PRODUCES_MAP = new HashMap<>();
	static
	{
		for(OsmItem item : PRODUCES) PRODUCES_MAP.put(item.osmValue, item);
	}

	protected int getItemsPerRow() { return 3; }
	protected int getMaxNumberOfInitiallyShownItems() { return -1; }

	@Override protected OsmItem[] getItems()
	{
		// only include what is given for that country
		ArrayList<OsmItem> result = new ArrayList<>();
		for(String name : getCountryInfo().getOrchardProduces())
		{
			OsmItem item = PRODUCES_MAP.get(name);
			if(item != null) result.add(item);
		}
		return result.toArray(new OsmItem[result.size()]);
	}
}
