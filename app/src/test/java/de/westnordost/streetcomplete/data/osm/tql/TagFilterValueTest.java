package de.westnordost.streetcomplete.data.osm.tql;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.OsmNode;

import static org.junit.Assert.*;

public class TagFilterValueTest
{
	@Test public void matchesEqual()
	{
		TagFilterValue eq = new TagFilterValue("highway", "=", "residential");

		assertTrue(eq.matches(elementWithTag("highway", "residential")));
		assertFalse(eq.matches(elementWithTag("highway", "residental")));
		assertFalse(eq.matches(new OsmNode(0, 0, 0d, 0d, null)));
	}

	@Test public void matchesNEqual()
	{
		TagFilterValue neq = new TagFilterValue("highway", "!=", "residential");

		assertFalse(neq.matches(elementWithTag("highway", "residential")));
		assertTrue(neq.matches(elementWithTag("highway", "residental")));
		assertTrue(neq.matches(new OsmNode(0, 0, 0d, 0d, null)));
	}

	@Test public void matchesLikeDot()
	{
		TagFilterValue like = new TagFilterValue("highway", "~", ".esidential");

		assertTrue(like.matches(elementWithTag("highway", "residential")));
		assertTrue(like.matches(elementWithTag("highway", "wesidential")));
		assertFalse(like.matches(elementWithTag("highway", "rresidential")));
		assertFalse(like.matches(new OsmNode(0, 0, 0d, 0d, null)));
	}

	@Test public void matchesLikeOr()
	{
		TagFilterValue like = new TagFilterValue("highway", "~", "residential|unclassified");

		assertTrue(like.matches(elementWithTag("highway", "residential")));
		assertTrue(like.matches(elementWithTag("highway", "unclassified")));
		assertFalse(like.matches(elementWithTag("highway", "blub")));
		assertFalse(like.matches(new OsmNode(0, 0, 0d, 0d, null)));
	}

	@Test public void matchesNotLikeDot()
	{
		TagFilterValue notlike = new TagFilterValue("highway", "!~", ".*");

		assertFalse(notlike.matches(elementWithTag("highway", "anything")));
		assertTrue(notlike.matches(new OsmNode(0, 0, 0d, 0d, null)));
	}

	@Test public void matchesNotLikeSomething()
	{
		TagFilterValue notlike = new TagFilterValue("noname", "!~", "yes");

		assertFalse(notlike.matches(elementWithTag("noname", "yes")));
		assertTrue(notlike.matches(elementWithTag("noname", "no")));
		assertTrue(notlike.matches(new OsmNode(0, 0, 0d, 0d, null)));
	}

	@Test public void matchesNoValue()
	{
		TagFilterValue key = new TagFilterValue("name", null, null);

		assertTrue(key.matches(elementWithTag("name", "yes")));
		assertTrue(key.matches(elementWithTag("name", "no")));
		assertFalse(key.matches(new OsmNode(0, 0, 0d, 0d, null)));
	}

	@Test public void toStringWorks()
	{
		TagFilterValue key = new TagFilterValue("A", "=", "B");
		assertEquals("\"A\"=\"B\"", key.toString());
		assertEquals("[\"A\"=\"B\"]", key.toOverpassQLString());
	}

	@Test public void toRegexString()
	{
		TagFilterValue key = new TagFilterValue("A", "~", "B");
		assertEquals("\"A\"~\"B\"", key.toString());
		assertEquals("[\"A\"~\"^(B)$\"]", key.toOverpassQLString());
	}

	@Test public void toRegexDotToString()
	{
		TagFilterValue key = new TagFilterValue("A", "!~", ".*");
		assertEquals("\"A\"!~\".*\"", key.toString());
		assertEquals("[\"A\"!~\".\"]", key.toOverpassQLString());
	}

	private Element elementWithTag(String key, String value)
	{
		Map<String,String> tags = new HashMap<>();
		tags.put(key, value);
		return new OsmNode(0, 0, 0d, 0d, tags);
	}
}
