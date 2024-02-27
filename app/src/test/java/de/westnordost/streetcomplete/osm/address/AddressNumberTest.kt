package de.westnordost.streetcomplete.osm.address

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import kotlin.test.Test
import kotlin.test.assertEquals

class AddressNumberTest {
    @Test fun `applyTo house number`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("addr:housenumber", "15")
            ),
            HouseNumber("15").appliedTo(mapOf())
        )
    }

    @Test fun `applyTo conscription number`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("addr:conscriptionnumber", "15"),
                StringMapEntryAdd("addr:housenumber", "15"),
            ),
            ConscriptionNumber("15").appliedTo(mapOf())
        )

        assertEquals(
            setOf(
                StringMapEntryAdd("addr:conscriptionnumber", "5"),
                StringMapEntryAdd("addr:housenumber", "1"),
                StringMapEntryAdd("addr:streetnumber", "1"),
            ),
            ConscriptionNumber("5", "1").appliedTo(mapOf())
        )
    }

    @Test fun `applyTo house and block number`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("addr:housenumber", "3"),
                StringMapEntryAdd("addr:block_number", "5"),
            ),
            HouseAndBlockNumber("3", "5").appliedTo(mapOf())
        )
    }

    @Test fun `applyTo house number clears other address number fields`() {
        assertEquals(
            setOf(
                StringMapEntryModify("addr:housenumber", "100", "123"),
                StringMapEntryDelete("addr:conscriptionnumber", "ABC"),
                StringMapEntryDelete("addr:streetnumber", "45"),
                StringMapEntryDelete("addr:block_number", "12"),
                StringMapEntryDelete("addr:block", "F"),
            ),
            HouseNumber("123").appliedTo(mapOf(
                "addr:housenumber" to "100",
                "addr:conscriptionnumber" to "ABC",
                "addr:streetnumber" to "45",
                "addr:block_number" to "12",
                "addr:block" to "F",
            ))
        )
    }

    @Test fun `applyTo house and block number clears other address number fields`() {
        assertEquals(
            setOf(
                StringMapEntryModify("addr:housenumber", "100", "123"),
                StringMapEntryDelete("addr:conscriptionnumber", "ABC"),
                StringMapEntryDelete("addr:streetnumber", "45"),
                StringMapEntryModify("addr:block_number", "12", "4"),
                StringMapEntryDelete("addr:block", "F"),
            ),
            HouseAndBlockNumber("123", "4").appliedTo(mapOf(
                "addr:housenumber" to "100",
                "addr:conscriptionnumber" to "ABC",
                "addr:streetnumber" to "45",
                "addr:block_number" to "12",
                "addr:block" to "F",
            ))
        )
    }

    @Test fun `applyTo house number and block clears other address number fields`() {
        assertEquals(
            setOf(
                StringMapEntryModify("addr:housenumber", "100", "123"),
                StringMapEntryDelete("addr:conscriptionnumber", "ABC"),
                StringMapEntryDelete("addr:streetnumber", "45"),
                StringMapEntryDelete("addr:block_number", "12"),
                StringMapEntryModify("addr:block", "F", "G"),
            ),
            HouseNumberAndBlock("123", "G").appliedTo(mapOf(
                "addr:housenumber" to "100",
                "addr:conscriptionnumber" to "ABC",
                "addr:streetnumber" to "45",
                "addr:block_number" to "12",
                "addr:block" to "F",
            ))
        )
    }

    @Test fun `applyTo conscription number clears other address number fields`() {
        assertEquals(
            setOf(
                StringMapEntryModify("addr:housenumber", "100", "123"),
                StringMapEntryModify("addr:conscriptionnumber", "ABC", "12345"),
                StringMapEntryModify("addr:streetnumber", "45", "123"),
                StringMapEntryDelete("addr:block_number", "12"),
                StringMapEntryDelete("addr:block", "F"),
            ),
            ConscriptionNumber("12345", "123").appliedTo(mapOf(
                "addr:housenumber" to "100",
                "addr:conscriptionnumber" to "ABC",
                "addr:streetnumber" to "45",
                "addr:block_number" to "12",
                "addr:block" to "F",
            ))
        )
    }
}

private fun AddressNumber.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}
