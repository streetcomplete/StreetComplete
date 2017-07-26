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
			new OsmItem("landuse=farmland;crop=sisal",		R.drawable.surface_grass, R.string.produce_sisal),

			new OsmItem("landuse=vineyard",			R.drawable.surface_grass, R.string.produce_grapes),

			new OsmItem("trees=agave_plants",		R.drawable.surface_grass, R.string.produce_agaves),
			new OsmItem("trees=almond_trees",		R.drawable.surface_grass, R.string.produce_almonds),
			new OsmItem("trees=apple_trees",		R.drawable.surface_grass, R.string.produce_apples),
			new OsmItem("trees=apricot_trees",		R.drawable.surface_grass, R.string.produce_apricots),
			new OsmItem("trees=avocado_trees",		R.drawable.surface_grass, R.string.produce_avocados),
			new OsmItem("trees=banana_plants",		R.drawable.surface_grass, R.string.produce_bananas),
			new OsmItem("trees=blueberry",			R.drawable.surface_grass, R.string.produce_blueberries),
			new OsmItem("trees=cacao_trees",		R.drawable.surface_grass, R.string.produce_cacao),
			new OsmItem("trees=cashew_trees",		R.drawable.surface_grass, R.string.produce_cashew_nuts),
			new OsmItem("trees=cherry_trees",		R.drawable.surface_grass, R.string.produce_cherries),
			new OsmItem("trees=chestnut_trees",		R.drawable.surface_grass, R.string.produce_chestnuts),
			new OsmItem("trees=coconut_palms",		R.drawable.surface_grass, R.string.produce_coconuts),
			new OsmItem("trees=coffee_plants",		R.drawable.surface_grass, R.string.produce_coffee),
			new OsmItem("trees=cranberry",			R.drawable.surface_grass, R.string.produce_cranberries),
			new OsmItem("trees=date_palms",			R.drawable.surface_grass, R.string.produce_dates),
			new OsmItem("trees=fig_trees",			R.drawable.surface_grass, R.string.produce_figs),
			new OsmItem("trees=grapefruit_trees",	R.drawable.surface_grass, R.string.produce_grapefruits),
			new OsmItem("trees=guava_trees",		R.drawable.surface_grass, R.string.produce_guavas),
			new OsmItem("trees=hazel_plants",		R.drawable.surface_grass, R.string.produce_hazelnuts),
			new OsmItem("trees=hop_plants",			R.drawable.surface_grass, R.string.produce_hops),
			new OsmItem("trees=jojoba_plants",		R.drawable.surface_grass, R.string.produce_jojoba),
			new OsmItem("trees=kiwi_plants",		R.drawable.surface_grass, R.string.produce_kiwis),
			new OsmItem("trees=kola_trees",			R.drawable.surface_grass, R.string.produce_kola_nuts),
			new OsmItem("trees=lemon_trees",		R.drawable.surface_grass, R.string.produce_lemons),
			new OsmItem("trees=lime_trees",			R.drawable.surface_grass, R.string.produce_limes),
			new OsmItem("trees=mango_trees",		R.drawable.surface_grass, R.string.produce_mangos),
			new OsmItem("trees=mate_plants",		R.drawable.surface_grass, R.string.produce_mate),
			// TODO: "nut"?
			new OsmItem("trees=nutmeg_trees",		R.drawable.surface_grass, R.string.produce_nutmeg),
			new OsmItem("trees=oil_palms",			R.drawable.surface_grass, R.string.produce_oil_palms),
			new OsmItem("trees=olive_trees",		R.drawable.surface_grass, R.string.produce_olives),
			new OsmItem("trees=orange_trees",		R.drawable.surface_grass, R.string.produce_oranges),
			// TODO: "palm"?
			new OsmItem("trees=papaya_trees",		R.drawable.surface_grass, R.string.produce_papayas),
			new OsmItem("trees=peach_trees",		R.drawable.surface_grass, R.string.produce_peaches),
			new OsmItem("trees=pear_trees",			R.drawable.surface_grass, R.string.produce_pears),
			// TODO: "pepper plant"? (chilis, bell pepper?)
			new OsmItem("???????????",				R.drawable.surface_grass, R.string.produce_chili),
			new OsmItem("trees=persimmon_trees",	R.drawable.surface_grass, R.string.produce_persimmons),
			new OsmItem("trees=pineapple_plants", 	R.drawable.surface_grass, R.string.produce_pineapples),
			new OsmItem("trees=piper_plants",		R.drawable.surface_grass, R.string.produce_pepper),
			new OsmItem("trees=pistachio_trees",	R.drawable.surface_grass, R.string.produce_pistachios),
			new OsmItem("trees=plum_trees",			R.drawable.surface_grass, R.string.produce_plums),
			new OsmItem("trees=raspberry",			R.drawable.surface_grass, R.string.produce_raspberries),
			new OsmItem("trees=rubber_trees",		R.drawable.surface_grass, R.string.produce_rubber),
			new OsmItem("trees=strawberry",			R.drawable.surface_grass, R.string.produce_strawberries),
			new OsmItem("trees=tea_plants",			R.drawable.surface_grass, R.string.produce_tea),
			new OsmItem("trees=vanilla_plants",		R.drawable.surface_grass, R.string.produce_vanilla),
			new OsmItem("trees=walnut_trees",		R.drawable.surface_grass, R.string.produce_walnuts),
			// TODO: jute?!
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
