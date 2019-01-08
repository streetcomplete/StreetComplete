package de.westnordost.streetcomplete.quests.add_housenumber

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.AOsmElementQuestTypeTest
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumber
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumberForm
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

import org.mockito.Mockito.mock


class AddHousenumberTest : AOsmElementQuestTypeTest() {

    override val questType = AddHousenumber(mock(OverpassMapDataDao::class.java))

    @Test fun regex() {
        val r = AddHousenumberForm.VALID_HOUSENUMBER_REGEX.toRegex()
        assertTrue("1".matches(r))
        assertTrue("1234".matches(r))

        assertTrue("1234a".matches(r))
        assertTrue("1234/a".matches(r))
        assertTrue("1234 / a".matches(r))
        assertTrue("1234 / A".matches(r))
        assertTrue("1234A".matches(r))
        assertTrue("1234/9".matches(r))
        assertTrue("1234 / 9".matches(r))

        assertFalse("12345".matches(r))
        assertFalse("1234 5".matches(r))
        assertFalse("1234/55".matches(r))
        assertFalse("1234AB".matches(r))
    }

    @Test fun number() {
        bundle.putString(AddHousenumberForm.HOUSENUMBER, "99b")
        verify(StringMapEntryAdd("addr:housenumber", "99b"))
    }

    @Test fun name() {
        bundle.putString(AddHousenumberForm.HOUSENAME, "La Escalera")
        verify(StringMapEntryAdd("addr:housename", "La Escalera"))
    }

    @Test fun conscriptionNumber() {
        bundle.putString(AddHousenumberForm.CONSCRIPTIONNUMBER, "I.123")
        verify(
            StringMapEntryAdd("addr:conscriptionnumber", "I.123"),
            StringMapEntryAdd("addr:housenumber", "I.123")
        )
    }

    @Test fun conscriptionNumberAndStreetNumber() {
        bundle.putString(AddHousenumberForm.CONSCRIPTIONNUMBER, "I.123")
        bundle.putString(AddHousenumberForm.STREETNUMBER, "12b")
        verify(
            StringMapEntryAdd("addr:conscriptionnumber", "I.123"),
            StringMapEntryAdd("addr:streetnumber", "12b"),
            StringMapEntryAdd("addr:housenumber", "12b")
        )
    }
}
