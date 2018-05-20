package de.westnordost.streetcomplete.quests.construction;

import junit.framework.TestCase;

import java.text.ParseException;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.streetcomplete.data.OsmModule;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AssertUtil;

public class MarkCompletedBuildingConstructionIntegrationTest extends TestCase {
	public void test_matching_candidate_is_accepted() throws ParseException {
		//https://www.openstreetmap.org/way/494183785#map=19/50.07671/19.94703
		verifyYieldsQuest(
			new BoundingBox(50.07664, 19.94671, 50.07672, 19.94700),
			"2018-03-01"
		);
	}

	public void test_fresh_construction_is_not_accepted() throws ParseException {
		//https://www.openstreetmap.org/way/494183785#map=19/50.07671/19.94703
		verifyYieldsNoQuest(
			new BoundingBox(50.07664, 19.94671, 50.07672, 19.94700),
			"2017-07-30"
		);
	}

	public void test_relations_are_accepted() throws ParseException {
		//https://www.openstreetmap.org/relation/7405013
		verifyYieldsQuest(
			new BoundingBox(55.89375, 37.53794, 55.89441, 37.53857),
			"2018-03-01"
		);
	}

	private void verifyYieldsNoQuest(BoundingBox bbox, String date) throws ParseException {
		OverpassMapDataDao o = OsmModule.overpassOldMapDataDao(OsmModule::overpassMapDataParser, date);
		MarkCompletedBuildingConstructionOldData quest = new MarkCompletedBuildingConstructionOldData(o, date);
		AssertUtil.verifyYieldsNoQuest(quest, bbox);
	}

	private void verifyYieldsQuest(BoundingBox bbox, String date) throws ParseException {
		OverpassMapDataDao o = OsmModule.overpassOldMapDataDao(OsmModule::overpassMapDataParser, date);
		MarkCompletedBuildingConstructionOldData quest = new MarkCompletedBuildingConstructionOldData(o, date);
		new AssertUtil().verifyYieldsQuest(quest, bbox);
	}
}
