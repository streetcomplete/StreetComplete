package de.westnordost.streetcomplete.quests.construction;

import junit.framework.TestCase;

import java.text.ParseException;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.streetcomplete.data.OsmModule;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AssertUtil;

public class MarkCompletedHighwayConstructionIntegrationTest extends TestCase {
	public void test_old_highway_construction_triggers_quest() throws ParseException {
		//https://www.openstreetmap.org/way/298656945 edited on 2014-08-18
		verifyYieldsQuest(
			new BoundingBox(40.01422, -3.02250, 40.01694, -3.02134),
			"2018-03-10"
		);
	}

	public void test_new_highway_construction_is_not_triggering_quest() throws ParseException {
		//https://www.openstreetmap.org/way/298656945 edited on 2014-08-18
		verifyYieldsNoQuest(
			new BoundingBox(40.01422, -3.02250, 40.01694, -3.02134),
			"2014-08-20"
		);
	}

	public void test_opening_date_tag_used_to_filter_out_active_construction() throws ParseException {
		//https://www.openstreetmap.org/way/22462987 - 2017-06-30 not generating, 2017-07-01 generating quest
		verifyYieldsNoQuest(
			new BoundingBox(47.80952, 12.09730, 47.81005, 12.09801),
			"2017-06-30"
		);
	}
	public void test_opening_date_tag_ignored_if_outdated() throws ParseException {
		//https://www.openstreetmap.org/way/22462987 - 2017-06-30 not generating, 2017-07-01 generating quest
		verifyYieldsQuest(
			new BoundingBox(47.80952, 12.09730, 47.81005, 12.09801),
			"2017-07-01"
		);
	}


	private void verifyYieldsNoQuest(BoundingBox bbox, String date) throws ParseException {
		OverpassMapDataDao o = OsmModule.overpassOldMapDataDao(OsmModule::overpassMapDataParser, date);
		MarkCompletedHighwayConstructionOldData quest = new MarkCompletedHighwayConstructionOldData(o, date);
		AssertUtil.verifyYieldsNoQuest(quest, bbox);
	}

	private void verifyYieldsQuest(BoundingBox bbox, String date) throws ParseException {
		OverpassMapDataDao o = OsmModule.overpassOldMapDataDao(OsmModule::overpassMapDataParser, date);
		MarkCompletedHighwayConstructionOldData quest = new MarkCompletedHighwayConstructionOldData(o, date);
		new AssertUtil().verifyYieldsQuest(quest, bbox);
	}
}
