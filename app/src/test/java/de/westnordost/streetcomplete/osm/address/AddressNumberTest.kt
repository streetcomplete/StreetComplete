package de.westnordost.streetcomplete.osm.address

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
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
}

// TODO what about if the address type changes -> old tags must be removed´!

private fun AddressNumber.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}
