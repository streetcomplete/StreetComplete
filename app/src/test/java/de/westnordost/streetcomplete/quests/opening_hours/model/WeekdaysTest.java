package de.westnordost.streetcomplete.quests.opening_hours.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class WeekdaysTest
{
	// hihi
	private static final boolean l = true;
	private static final boolean o = false;

	@Test public void intersects() {
		assertFalse(
			new Weekdays(new boolean[]{l,o,l,o,l,o,l}).intersects(
			new Weekdays(new boolean[]{o,l,o,l,o,l,o})
		));
		assertTrue(
			new Weekdays(new boolean[]{o,o,o,o,o,o,l}).intersects(
			new Weekdays(new boolean[]{o,o,o,o,o,o,l})
		));
	}

	@Test public void isSelectionEmpty() {
		assertTrue(new Weekdays(new boolean[8]).isSelectionEmpty());
		assertTrue(new Weekdays(new boolean[]{o,o,o,o,o,o,o,o}).isSelectionEmpty());
		assertFalse(new Weekdays(new boolean[]{o,o,o,l,o,o,o,o}).isSelectionEmpty());
	}

	@Test public void toStringWorks() {
		assertEquals("Mo,Tu", new Weekdays(new boolean[]{l,l,o,o,o,o,o,o}).toString());
		assertEquals("Tu-Th", new Weekdays(new boolean[]{o,l,l,l,o,o,o,o}).toString());
		assertEquals("Tu-Th,Sa,Su,PH", new Weekdays(new boolean[]{o,l,l,l,o,l,l,l}).toString());
		assertEquals("Su,Mo", new Weekdays(new boolean[]{l,o,o,o,o,o,l,o}).toString());
		assertEquals("Sa-Th,PH", new Weekdays(new boolean[]{l,l,l,l,o,l,l,l}).toString());
	}
}
