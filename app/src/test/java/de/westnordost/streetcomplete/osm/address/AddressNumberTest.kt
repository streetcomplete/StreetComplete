package de.westnordost.streetcomplete.osm.address

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import org.assertj.core.api.Assertions
import org.junit.Test

class AddressNumberTest {
    @Test fun `applyTo house number`() {
        verifyAnswer(
            HouseNumber("15"),
            arrayOf(
                StringMapEntryAdd("addr:housenumber", "15")
            )
        )
    }

    @Test fun `applyTo conscription number`() {
        verifyAnswer(
            ConscriptionNumber("15"),
            arrayOf(
                StringMapEntryAdd("addr:conscriptionnumber", "15"),
                StringMapEntryAdd("addr:housenumber", "15"),
            )
        )

        verifyAnswer(
            ConscriptionNumber("5", "1"),
            arrayOf(
                StringMapEntryAdd("addr:conscriptionnumber", "5"),
                StringMapEntryAdd("addr:housenumber", "1"),
                StringMapEntryAdd("addr:streetnumber", "1"),
            )
        )
    }

    @Test fun `applyTo house and block number`() {
        verifyAnswer(
            HouseAndBlockNumber("3", "5"),
            arrayOf(
                StringMapEntryAdd("addr:housenumber", "3"),
                StringMapEntryAdd("addr:block_number", "5"),
            )
        )
    }
}

private fun verifyAnswer(answer: AddressNumber, expectedChanges: Array<StringMapEntryChange>) {
    val cb = StringMapChangesBuilder(emptyMap())
    answer.applyTo(cb)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}
