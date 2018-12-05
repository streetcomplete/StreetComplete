package de.westnordost.streetcomplete.quests.opening_hours.model;


import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class NumberSystemTest
{
	@Test(expected = IllegalArgumentException.class) public void illegalArguments()
	{
		new NumberSystem(10,3);
	}

	@Test public void getSize()
	{
		NumberSystem s = new NumberSystem(5,10);
		assertEquals(5,s.getSize(new CircularSection(2,9)));
		assertEquals(1,s.getSize(new CircularSection(5,5)));
		assertEquals(2,s.getSize(new CircularSection(5,6)));
		assertEquals(6,s.getSize(new CircularSection(6,5)));
		assertEquals(3,s.getSize(new CircularSection(9,5)));
	}

	@Test public void complementNone()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complemented(new ArrayList<>());
		assertEquals(1,r.size());
		assertEquals(new CircularSection(0,10),r.get(0));
	}

	@Test public void complementFull()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complemented(Collections.singletonList(new CircularSection(0,10)));
		assertTrue(r.isEmpty());
	}

	@Test public void complementOneAtEnd()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complemented(Collections.singletonList(new CircularSection(0,8)));
		assertEquals(1,r.size());
		assertEquals(new CircularSection(9,10),r.get(0));
	}

	@Test public void complementSqueezeOneAtEnd()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complemented(Collections.singletonList(new CircularSection(0,9)));
		assertEquals(1,r.size());
		assertEquals(new CircularSection(10,10),r.get(0));
	}

	@Test public void complementOneAtStart()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complemented(Collections.singletonList(new CircularSection(4,10)));
		assertEquals(1,r.size());
		assertEquals(new CircularSection(0,3),r.get(0));
	}

	@Test public void complementSqueezeOneAtStart()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complemented(Collections.singletonList(new CircularSection(1,10)));
		assertEquals(1,r.size());
		assertEquals(new CircularSection(0,0),r.get(0));
	}

	@Test public void complementOneCenter()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complemented(Arrays.asList(
				new CircularSection(0, 3), new CircularSection(6, 10)));
		assertEquals(1,r.size());
		assertEquals(new CircularSection(4,5),r.get(0));
	}

	@Test public void complementBothEnds()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complemented(Collections.singletonList(new CircularSection(3, 8)));
		assertEquals(1,r.size());
		assertEquals(new CircularSection(9,2), r.get(0));
	}

	@Test public void complementSwissCheese()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complemented(Arrays.asList(
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

	@Test public void complementLoop()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complemented(Collections.singletonList(new CircularSection(8, 5)));
		assertEquals(1, r.size());
		assertEquals(new CircularSection(6,7), r.get(0));
	}

	@Test public void noComplementAtEnd()
	{
		NumberSystem s = new NumberSystem(0,10);
		List<CircularSection> r = s.complemented(Arrays.asList(
				new CircularSection(0,9),
				new CircularSection(10,10)
		));
		assertTrue(r.isEmpty());
	}

	@Test public void merge()
	{
		NumberSystem s = new NumberSystem(3,10);
		List<CircularSection> sections = new ArrayList<>();
		sections.add(new CircularSection(3,4));
		sections.add(new CircularSection(9,10));
		List<CircularSection> result = s.merged(sections);

		assertEquals(1, result.size());
		assertEquals(9,result.get(0).getStart());
		assertEquals(4,result.get(0).getEnd());
	}
}
