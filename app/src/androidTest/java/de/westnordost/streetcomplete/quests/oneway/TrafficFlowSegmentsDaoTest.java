package de.westnordost.streetcomplete.quests.oneway;

import junit.framework.TestCase;

import org.json.JSONException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.OsmLatLon;

import static de.westnordost.streetcomplete.data.OsmModule.ONEWAY_API_URL;
import static org.assertj.core.api.Assertions.*;

public class TrafficFlowSegmentsDaoTest extends TestCase
{
	public void testParseEmptyDoesNotResultInError() throws JSONException
	{
		Map<Long, List<TrafficFlowSegment>> result = TrafficFlowSegmentsDao.parse("{\"segments\":[]}");
		assertTrue(result.isEmpty());
	}

	public void testParseTwoOfDifferentWay() throws JSONException
	{
		Map<Long, List<TrafficFlowSegment>> result = TrafficFlowSegmentsDao.parse(
			"{\"segments\":[" +
			"{\"wayId\":1,\"fromPosition\":{\"lon\":1, \"lat\":2},\"toPosition\":{\"lon\":5, \"lat\":6}}," +
			"{\"wayId\":2,\"fromPosition\":{\"lon\":3, \"lat\":4},\"toPosition\":{\"lon\":7, \"lat\":8}}," +
			"]}"
		);
		Map<Long, List<TrafficFlowSegment>> expected = new HashMap<>();
		expected.put(1L, Collections.singletonList(
			new TrafficFlowSegment(new OsmLatLon(2,1), new OsmLatLon(6,5))
		));
		expected.put(2L, Collections.singletonList(new TrafficFlowSegment(
			new OsmLatLon(4,3), new OsmLatLon(8,7))
		));
		assertThat(result).containsAllEntriesOf(expected);
	}

	public void testParseTwoOfSameWay() throws JSONException
	{
		Map<Long, List<TrafficFlowSegment>> result = TrafficFlowSegmentsDao.parse(
			"{\"segments\":[" +
			"{\"wayId\":1,\"fromPosition\":{\"lon\":1, \"lat\":2},\"toPosition\":{\"lon\":5, \"lat\":6}}," +
			"{\"wayId\":1,\"fromPosition\":{\"lon\":3, \"lat\":4},\"toPosition\":{\"lon\":7, \"lat\":8}}," +
			"]}"
		);
		Map<Long, List<TrafficFlowSegment>> expected = new HashMap<>();
		expected.put(1L, Arrays.asList(
			new TrafficFlowSegment(new OsmLatLon(2,1), new OsmLatLon(6,5)),
			new TrafficFlowSegment(new OsmLatLon(4,3), new OsmLatLon(8,7))
		));
		assertThat(result).containsAllEntriesOf(expected);
	}

	public void testWithSomeRealData() throws Exception
	{
		// should just not crash...
		new TrafficFlowSegmentsDao(ONEWAY_API_URL).get(new BoundingBox(-34,18,-33,19));
	}
}
