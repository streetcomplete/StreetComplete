package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import de.westnordost.streetcomplete.quests.opening_hours.model.*
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Before
import org.junit.Test


class AddOpeningHoursTest {

    private lateinit var parser: OpeningHoursTagParser
    private lateinit var questType: AddOpeningHours

    @Before fun setUp() {
        parser = mock()
        questType = AddOpeningHours(mock(), mock(), parser)
    }

    @Test fun `apply description answer`() {
        questType.verifyAnswer(
            DescribeOpeningHours("my cool \"opening\" hours"),
            StringMapEntryAdd("opening_hours", "\"my cool opening hours\"")
        )
    }

    @Test fun `apply no opening hours sign answer`() {
        questType.verifyAnswer(
            NoOpeningHoursSign,
            StringMapEntryAdd("opening_hours:signed", "no")
        )
    }

    @Test fun `apply always open answer`() {
        questType.verifyAnswer(
            AlwaysOpen,
            StringMapEntryAdd("opening_hours", "24/7")
        )
    }

    @Test fun `apply opening hours answer`() {
        val input: List<OpeningMonthsRow> = mock()
        on(parser.internalFlatIntoTag(input)).thenReturn("blubbi blob")
        questType.verifyAnswer(
            RegularOpeningHours(input),
            StringMapEntryAdd("opening_hours", "blubbi blob")
        )
    }
}
