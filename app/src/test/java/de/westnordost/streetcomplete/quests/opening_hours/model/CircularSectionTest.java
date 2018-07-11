package de.westnordost.streetcomplete.quests.opening_hours.model;

import junit.framework.TestCase;

import de.westnordost.streetcomplete.quests.opening_hours.model.CircularSection;

public class CircularSectionTest extends TestCase
{
	public void testStartEnd()
	{
		CircularSection cs = new CircularSection(0,10);
		assertEquals(0, cs.getStart());
		assertEquals(10, cs.getEnd());
		assertFalse(cs.loops());
	}

	public void testLoops()
	{
		CircularSection cs = new CircularSection(10,0);
		assertTrue(cs.loops());
	}

	public void testIntersect()
	{
		CircularSection cs = new CircularSection(0,10);
		CircularSection tooHigh = new CircularSection(11,12);
		CircularSection tooLow = new CircularSection(11,12);
		CircularSection touchesUpperEnd = new CircularSection(10,10);
		CircularSection touchesLowerEnd = new CircularSection(-1,0);
		CircularSection contains = new CircularSection(-10,20);
		CircularSection intersectsLowerSection = new CircularSection(-10,3);
		CircularSection intersectUpperSection = new CircularSection(8,20);
		CircularSection loopsOutside = new CircularSection(11,-1);
		CircularSection loopsIntersectsLowerSection = new CircularSection(11,3);
		CircularSection loopsIntersectsUpperSection = new CircularSection(8,-5);

		assertTrue(cs.intersects(cs));
		assertFalse(cs.intersects(tooHigh));
		assertFalse(cs.intersects(tooLow));
		assertTrue(cs.intersects(touchesLowerEnd));
		assertTrue(cs.intersects(touchesUpperEnd));
		assertTrue(cs.intersects(contains));
		assertTrue(cs.intersects(intersectsLowerSection));
		assertTrue(cs.intersects(intersectUpperSection));
		assertFalse(cs.intersects(loopsOutside));
		assertTrue(cs.intersects(loopsIntersectsLowerSection));
		assertTrue(cs.intersects(loopsIntersectsUpperSection));
		assertTrue(loopsIntersectsLowerSection.intersects(loopsIntersectsUpperSection));
	}

	public void testCompare()
	{
		CircularSection looper = new CircularSection(10,0);
		CircularSection lowStart = new CircularSection(0,10);
		CircularSection lowStartButHighEnd = new CircularSection(0,50);
		CircularSection highStart = new CircularSection(10,20);

		assertTrue(looper.compareTo(lowStart) < 0);
		assertTrue(looper.compareTo(lowStartButHighEnd) < 0);
		assertTrue(looper.compareTo(highStart) < 0);
		assertTrue(lowStart.compareTo(looper) > 0);
		assertTrue(lowStartButHighEnd.compareTo(looper) > 0);
		assertTrue(highStart.compareTo(looper) > 0);

		assertTrue(lowStart.compareTo(lowStartButHighEnd) < 0);
		assertTrue(lowStart.compareTo(highStart) < 0);
		assertTrue(lowStartButHighEnd.compareTo(lowStart) > 0);
		assertTrue(highStart.compareTo(lowStart) > 0);

		assertTrue(lowStartButHighEnd.compareTo(highStart) < 0);
		assertTrue(highStart.compareTo(lowStartButHighEnd) > 0);
	}

	public void testToStringUsing()
	{
		String[] abc = new String[]{"a","b","c","d"};
		assertEquals("a",new CircularSection(0,0).toStringUsing(abc, "-"));
		assertEquals("a-d",new CircularSection(0,3).toStringUsing(abc, "-"));
		assertEquals("a-b",new CircularSection(0,1).toStringUsing(abc, "-"));
	}

	public void testEquals()
	{
		CircularSection cs = new CircularSection(0,10);
		assertNotNull(cs);
		assertEquals(cs, cs);
		assertFalse(cs.equals(new Object()));
		assertFalse(cs.equals(new CircularSection(10,0)));
		assertEquals(cs, new CircularSection(0,10));
	}

	public void testHashCodeIdentity()
	{
		assertEquals( new CircularSection(0,10).hashCode(), new CircularSection(0,10).hashCode());
	}

	public void testHashCodeNotTooSimple()
	{
		assertFalse( new CircularSection(0,10).hashCode() == new CircularSection(10,0).hashCode());
	}
}
