package de.westnordost.osmagent.quests.opening_hours;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NumberSystemTest extends TestCase
{
	public void testIllegal()
	{
		try
		{
			new NumberSystem(10,3);
			fail();
		}
		catch(Exception e) {}
	}

	public void testComplementNone()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complement(new ArrayList<CircularSection>());
		assertEquals(1,r.size());
		assertEquals(new CircularSection(0,10),r.get(0));
	}

	public void testComplementFull()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complement(Collections.singletonList(new CircularSection(0,10)));
		assertTrue(r.isEmpty());
	}

	public void testComplementOneAtEnd()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complement(Collections.singletonList(new CircularSection(0,8)));
		assertEquals(1,r.size());
		assertEquals(new CircularSection(9,10),r.get(0));
	}

	public void testComplementSqueezeOneAtEnd()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complement(Collections.singletonList(new CircularSection(0,9)));
		assertEquals(1,r.size());
		assertEquals(new CircularSection(10,10),r.get(0));
	}

	public void testComplementOneAtStart()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complement(Collections.singletonList(new CircularSection(4,10)));
		assertEquals(1,r.size());
		assertEquals(new CircularSection(0,3),r.get(0));
	}

	public void testComplementSqueezeOneAtStart()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complement(Collections.singletonList(new CircularSection(1,10)));
		assertEquals(1,r.size());
		assertEquals(new CircularSection(0,0),r.get(0));
	}

	public void testComplementOneCenter()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complement(Arrays.asList(
				new CircularSection(0, 3), new CircularSection(6, 10)));
		assertEquals(1,r.size());
		assertEquals(new CircularSection(4,5),r.get(0));
	}

	public void testComplementBothEnds()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complement(Collections.singletonList(new CircularSection(3, 8)));
		assertEquals(1,r.size());
		assertEquals(new CircularSection(9,2), r.get(0));
	}

	public void testComplementSwissCheese()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complement(Arrays.asList(
				new CircularSection(-5, 1),
				new CircularSection(3,3),
				new CircularSection(7,8)
				));
		assertEquals(3,r.size());
		assertTrue(r.containsAll(Arrays.asList(
				new CircularSection(2,2),
				new CircularSection(4,6),
				new CircularSection(9,10)
		)));
	}

	public void testComplementLoop()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complement(Collections.singletonList(new CircularSection(8, 5)));
		assertEquals(1, r.size());
		assertEquals(new CircularSection(6,7), r.get(0));
	}

	public void testNoComplementAtEnd()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complement(Arrays.asList(
				new CircularSection(0,9),
				new CircularSection(10,10)
		));
		assertTrue(r.isEmpty());
	}
}
