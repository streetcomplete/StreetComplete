package de.westnordost.streetcomplete.quests.add_housenumber

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.housenumber.*
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

import org.mockito.Mockito.mock


class AddHousenumberTest {

    private val questType = AddHousenumber(mock(OverpassMapDataDao::class.java))

    @Test fun `housenumber regex`() {
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

    @Test fun `blocknumber regex`() {
        val r = AddHousenumberForm.VALID_BLOCKNUMBER_REGEX.toRegex()
        assertTrue("1".matches(r))
        assertTrue("1234".matches(r))
        assertFalse("12345".matches(r))

        assertTrue("1234a".matches(r))
        assertTrue("1234 a".matches(r))
        assertFalse("1234 ab".matches(r))
    }

    @Test fun `apply house number answer`() {
        questType.verifyAnswer(
            HouseNumber("99b"),
            StringMapEntryAdd("addr:housenumber", "99b")
        )
    }

    @Test fun `apply house name answer`() {
        questType.verifyAnswer(
            HouseName("La Escalera"),
            StringMapEntryAdd("addr:housename", "La Escalera")
        )
    }

    @Test fun `apply conscription number answer`() {
        questType.verifyAnswer(
            ConscriptionNumber("I.123"),
            StringMapEntryAdd("addr:conscriptionnumber", "I.123"),
            StringMapEntryAdd("addr:housenumber", "I.123")
        )
    }

    @Test fun `apply conscription and street number answer`() {
        questType.verifyAnswer(
            ConscriptionNumber("I.123", "12b"),
            StringMapEntryAdd("addr:conscriptionnumber", "I.123"),
            StringMapEntryAdd("addr:streetnumber", "12b"),
            StringMapEntryAdd("addr:housenumber", "12b")
        )
    }

    @Test fun `apply block and house number answer`() {
        questType.verifyAnswer(
            HouseAndBlockNumber("12A", "123"),
            StringMapEntryAdd("addr:block_number", "123"),
            StringMapEntryAdd("addr:housenumber", "12A")
        )
    }

    @Test fun `apply no address answer`() {
        questType.verifyAnswer(
            NoAddress,
            StringMapEntryAdd("noaddress", "yes")
        )
    }
}
