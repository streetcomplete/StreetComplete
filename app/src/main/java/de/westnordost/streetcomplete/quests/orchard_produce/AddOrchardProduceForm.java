package de.westnordost.streetcomplete.quests.orchard_produce;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;

public class AddOrchardProduceForm extends ImageListQuestAnswerFragment
{
	private static final OsmItem[] PRODUCE = new OsmItem[]{
			// ordered alphabetically here for overview

			// may have been mistaken for an orchard (i.e. agave) from satellite imagery
			// landuse=farmland
			new OsmItem("sisal",		R.drawable.surface_grass, R.string.produce_sisal),
			// landuse=vineyard
			new OsmItem("grape",		R.drawable.surface_grass, R.string.produce_grapes),

			new OsmItem("agave",		R.drawable.surface_grass, R.string.produce_agaves),
			new OsmItem("almond",		R.drawable.surface_grass, R.string.produce_almonds),
			new OsmItem("apple",		R.drawable.surface_grass, R.string.produce_apples),
			new OsmItem("apricot",		R.drawable.surface_grass, R.string.produce_apricots),
			new OsmItem("avocado",		R.drawable.surface_grass, R.string.produce_avocados),
			new OsmItem("banana",		R.drawable.surface_grass, R.string.produce_bananas),
			new OsmItem("blueberry",	R.drawable.surface_grass, R.string.produce_blueberries),
			new OsmItem("cacao",		R.drawable.surface_grass, R.string.produce_cacao),
			new OsmItem("cashew",		R.drawable.surface_grass, R.string.produce_cashew_nuts),
			new OsmItem("cherry",		R.drawable.surface_grass, R.string.produce_cherries),
			new OsmItem("chestnut",		R.drawable.surface_grass, R.string.produce_chestnuts),
			new OsmItem("coconut",		R.drawable.surface_grass, R.string.produce_coconuts),
			new OsmItem("coffee",		R.drawable.surface_grass, R.string.produce_coffee),
			new OsmItem("cranberry",	R.drawable.surface_grass, R.string.produce_cranberries),
			new OsmItem("date",			R.drawable.surface_grass, R.string.produce_dates),
			new OsmItem("fig",			R.drawable.surface_grass, R.string.produce_figs),
			new OsmItem("grapefruit",	R.drawable.surface_grass, R.string.produce_grapefruits),
			new OsmItem("guava",		R.drawable.surface_grass, R.string.produce_guavas),
			new OsmItem("hazelnut",		R.drawable.surface_grass, R.string.produce_hazelnuts),
			new OsmItem("hop",			R.drawable.surface_grass, R.string.produce_hops),
			new OsmItem("jojoba",		R.drawable.surface_grass, R.string.produce_jojoba),
			new OsmItem("kiwi",			R.drawable.surface_grass, R.string.produce_kiwis),
			new OsmItem("kola_nut",		R.drawable.surface_grass, R.string.produce_kola_nuts),
			new OsmItem("lemon",		R.drawable.surface_grass, R.string.produce_lemons),
			new OsmItem("lime",			R.drawable.surface_grass, R.string.produce_limes),
			new OsmItem("mango",		R.drawable.surface_grass, R.string.produce_mangos),
			new OsmItem("mangosteen",	R.drawable.surface_grass, R.string.produce_mangosteen),
			new OsmItem("mate",			R.drawable.surface_grass, R.string.produce_mate),
			new OsmItem("nutmeg",		R.drawable.surface_grass, R.string.produce_nutmeg),
			new OsmItem("palm_oil",		R.drawable.surface_grass, R.string.produce_oil_palms),
			new OsmItem("olive",		R.drawable.surface_grass, R.string.produce_olives),
			new OsmItem("orange",		R.drawable.surface_grass, R.string.produce_oranges),
			new OsmItem("papaya",		R.drawable.surface_grass, R.string.produce_papayas),
			new OsmItem("peach",		R.drawable.surface_grass, R.string.produce_peaches),
			new OsmItem("pear",			R.drawable.surface_grass, R.string.produce_pears),
			new OsmItem("chili",		R.drawable.surface_grass, R.string.produce_chili),
			new OsmItem("persimmon",	R.drawable.surface_grass, R.string.produce_persimmons),
			new OsmItem("pineapple", 	R.drawable.surface_grass, R.string.produce_pineapples),
			new OsmItem("pepper",		R.drawable.surface_grass, R.string.produce_pepper),
			new OsmItem("pistachio",	R.drawable.surface_grass, R.string.produce_pistachios),
			new OsmItem("plum",			R.drawable.surface_grass, R.string.produce_plums),
			new OsmItem("raspberry",	R.drawable.surface_grass, R.string.produce_raspberries),
			new OsmItem("rubber",		R.drawable.surface_grass, R.string.produce_rubber),
			new OsmItem("strawberry",	R.drawable.surface_grass, R.string.produce_strawberries),
			new OsmItem("tea",			R.drawable.surface_grass, R.string.produce_tea),
			new OsmItem("tomatoe",		R.drawable.surface_grass, R.string.produce_tomatoes),
			new OsmItem("vanilla",		R.drawable.surface_grass, R.string.produce_vanilla),
			new OsmItem("walnut",		R.drawable.surface_grass, R.string.produce_walnuts),
	};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle(R.string.quest_orchard_produce_title);
		return view;
	}

	@Override protected OsmItem[] getItems()
	{
		// TODO add country intelligence
		return PRODUCE;
	}
}
