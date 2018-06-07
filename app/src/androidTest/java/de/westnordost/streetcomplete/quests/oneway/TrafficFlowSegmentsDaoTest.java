package de.westnordost.streetcomplete.quests.oneway;

import junit.framework.TestCase;

import org.json.JSONException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.BoundingBox;

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
				"{\"wayId\":1,\"fromNodeId\":7,\"toNodeId\":8}," +
				"{\"wayId\":2,\"fromNodeId\":5,\"toNodeId\":6}," +
				"]}"
		);
		Map<Long, List<TrafficFlowSegment>> expected = new HashMap<>();
		expected.put(1L, Collections.singletonList(new TrafficFlowSegment(7, 8)));
		expected.put(2L, Collections.singletonList(new TrafficFlowSegment(5, 6)));
		assertThat(result).containsAllEntriesOf(expected);
	}

	public void testParseTwoOfSameWay() throws JSONException
	{
		Map<Long, List<TrafficFlowSegment>> result = TrafficFlowSegmentsDao.parse(
			"{\"segments\":[" +
			"{\"wayId\":1,\"fromNodeId\":7,\"toNodeId\":8}," +
			"{\"wayId\":1,\"fromNodeId\":5,\"toNodeId\":6}," +
			"]}"
		);
		Map<Long, List<TrafficFlowSegment>> expected = new HashMap<>();
		expected.put(1L, Arrays.asList(new TrafficFlowSegment(7,8), new TrafficFlowSegment(5,6)));
		assertThat(result).containsAllEntriesOf(expected);
	}

	public void testWithSomeRealData() throws Exception
	{
		// should just not crash...
		new TrafficFlowSegmentsDao(ONEWAY_API_URL).get(new BoundingBox(-34,18,-33,19));
	}
}
