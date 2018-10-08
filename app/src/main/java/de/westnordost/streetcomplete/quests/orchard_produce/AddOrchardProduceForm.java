package de.westnordost.streetcomplete.quests.orchard_produce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;

public class AddOrchardProduceForm extends ImageListQuestAnswerFragment
{
	private static final Item[] PRODUCES = new Item[]{
			// ordered alphabetically here for overview

			// may have been mistaken for an orchard (i.e. agave) from satellite imagery
			// landuse=farmland
			new Item("sisal",		R.drawable.produce_sisal, R.string.produce_sisal),
			// landuse=vineyard
			new Item("grape",		R.drawable.produce_grape, R.string.produce_grapes),

			new Item("agave",		R.drawable.produce_agave, R.string.produce_agaves),
			new Item("almond",		R.drawable.produce_almond, R.string.produce_almonds),
			new Item("apple",		R.drawable.produce_apple, R.string.produce_apples),
			new Item("apricot",		R.drawable.produce_apricot, R.string.produce_apricots),
			new Item("areca_nut",	R.drawable.produce_areca_nut, R.string.produce_areca_nuts),
			new Item("avocado",		R.drawable.produce_avocado, R.string.produce_avocados),
			new Item("banana",		R.drawable.produce_banana, R.string.produce_bananas),
			new Item("sweet_pepper",R.drawable.produce_bell_pepper, R.string.produce_sweet_peppers),
			new Item("blueberry",	R.drawable.produce_blueberry, R.string.produce_blueberries),
			new Item("brazil_nut",	R.drawable.produce_brazil_nut, R.string.produce_brazil_nuts),
			new Item("cacao",		R.drawable.produce_cacao, R.string.produce_cacao),
			new Item("cashew",		R.drawable.produce_cashew, R.string.produce_cashew_nuts),
			new Item("cherry",		R.drawable.produce_cherry, R.string.produce_cherries),
			new Item("chestnut",	R.drawable.produce_chestnut, R.string.produce_chestnuts),
			new Item("chilli_pepper",R.drawable.produce_chili, R.string.produce_chili),
			new Item("coconut",		R.drawable.produce_coconut, R.string.produce_coconuts),
			new Item("coffee",		R.drawable.produce_coffee, R.string.produce_coffee),
			new Item("cranberry",	R.drawable.produce_cranberry, R.string.produce_cranberries),
			new Item("date",		R.drawable.produce_date, R.string.produce_dates),
			new Item("fig",			R.drawable.produce_fig, R.string.produce_figs),
			new Item("grapefruit",	R.drawable.produce_grapefruit, R.string.produce_grapefruits),
			new Item("guava",		R.drawable.produce_guava, R.string.produce_guavas),
			new Item("hazelnut",	R.drawable.produce_hazelnut, R.string.produce_hazelnuts),
			new Item("hop",			R.drawable.produce_hop, R.string.produce_hops),
			new Item("jojoba",		R.drawable.produce_jojoba, R.string.produce_jojoba),
			new Item("kiwi",		R.drawable.produce_kiwi, R.string.produce_kiwis),
			new Item("kola_nut",	R.drawable.produce_kola_nut, R.string.produce_kola_nuts),
			new Item("lemon",		R.drawable.produce_lemon, R.string.produce_lemons),
			new Item("lime",		R.drawable.produce_lime, R.string.produce_limes),
			new Item("mango",		R.drawable.produce_mango, R.string.produce_mangos),
			new Item("mangosteen",	R.drawable.produce_mangosteen, R.string.produce_mangosteen),
			new Item("mate",		R.drawable.produce_mate, R.string.produce_mate),
			new Item("nutmeg",		R.drawable.produce_nutmeg, R.string.produce_nutmeg),
			new Item("olive",		R.drawable.produce_olive, R.string.produce_olives),
			new Item("orange",		R.drawable.produce_orange, R.string.produce_oranges),
			new Item("palm_oil",	R.drawable.produce_palm_oil, R.string.produce_oil_palms),
			new Item("papaya",		R.drawable.produce_papaya, R.string.produce_papayas),
			new Item("peach",		R.drawable.produce_peach, R.string.produce_peaches),
			new Item("pear",		R.drawable.produce_pear, R.string.produce_pears),
			new Item("pepper",		R.drawable.produce_pepper, R.string.produce_pepper),
			new Item("persimmon",	R.drawable.produce_persimmon, R.string.produce_persimmons),
			new Item("pineapple", 	R.drawable.produce_pineapple, R.string.produce_pineapples),
			new Item("pistachio",	R.drawable.produce_pistachio, R.string.produce_pistachios),
			new Item("plum",		R.drawable.produce_plum, R.string.produce_plums),
			new Item("raspberry",	R.drawable.produce_raspberry, R.string.produce_raspberries),
			new Item("rubber",		R.drawable.produce_rubber, R.string.produce_rubber),
			new Item("strawberry",	R.drawable.produce_strawberry, R.string.produce_strawberries),
			new Item("tea",			R.drawable.produce_tea, R.string.produce_tea),
			new Item("tomato",		R.drawable.produce_tomato, R.string.produce_tomatoes),
			new Item("tung_nut",	R.drawable.produce_tung_nut, R.string.produce_tung_nuts),
			new Item("vanilla",		R.drawable.produce_vanilla, R.string.produce_vanilla),
			new Item("walnut",		R.drawable.produce_walnut, R.string.produce_walnuts),
	};
	private static final Map<String,Item> PRODUCES_MAP = new HashMap<>();
	static
	{
		for(Item item : PRODUCES) PRODUCES_MAP.put(item.value, item);
	}

	protected int getItemsPerRow() { return 3; }
	protected int getMaxNumberOfInitiallyShownItems() { return -1; }

	@Override protected Item[] getItems()
	{
		// only include what is given for that country
		ArrayList<Item> result = new ArrayList<>();
		for(String name : getCountryInfo().getOrchardProduces())
		{
			Item item = PRODUCES_MAP.get(name);
			if(item != null) result.add(item);
		}
		return result.toArray(new Item[result.size()]);
	}
}
