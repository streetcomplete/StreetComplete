package de.westnordost.streetcomplete.quests.existence

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import java.util.concurrent.FutureTask
import kotlin.test.assertEquals
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckExistenceTest {
    private val questType = CheckExistence(mockOfFeatureDictionary())

    private fun mockOfFeatureDictionary(): FutureTask<FeatureDictionary> {
        // another option is following CheckExistenceLabelTest
        // and doing it as an androidTest
        val matchFound: FeatureDictionary.QueryByTagBuilder = mock()
        on(matchFound.find()).thenReturn(listOf(mock()))
        on(matchFound.isSuggestion(false)).thenReturn(matchFound)

        val noMatches: FeatureDictionary.QueryByTagBuilder = mock()
        on(noMatches.find()).thenReturn(emptyList())
        on(noMatches.isSuggestion(false)).thenReturn(noMatches)

        val tagFinder: FeatureDictionary = mock()
        on(tagFinder.byTags(mapOf("amenity" to "telephone"))).thenReturn(matchFound)
        on(tagFinder.byTags(mapOf("shop" to "weird_value"))).thenReturn(noMatches)

        val tagFinderFutureTask: FutureTask<FeatureDictionary> = mock()
        on(tagFinderFutureTask.get()).thenReturn(tagFinder)
        return tagFinderFutureTask
    }

    @Test fun `apply answer adds check date`() {
        questType.verifyAnswer(
            Unit,
            StringMapEntryAdd("check_date", nowAsCheckDateString())
        )
    }

    @Test fun `apply answer removes all previous survey keys`() {
        questType.verifyAnswer(
            mapOf(
                "check_date" to "1",
                "lastcheck" to "a",
                "last_checked" to "b",
                "survey:date" to "c",
                "survey_date" to "d"
            ),
            Unit,
            StringMapEntryModify("check_date", "1", nowAsCheckDateString()),
            StringMapEntryDelete("lastcheck", "a"),
            StringMapEntryDelete("last_checked", "b"),
            StringMapEntryDelete("survey:date", "c"),
            StringMapEntryDelete("survey_date", "d"),
        )
    }

    @Test fun `isApplicableTo returns false for known places with recently edited amenity=telephone`() {
        assertFalse(
            questType.isApplicableTo(
                node(
                    tags = mapOf(
                        "amenity" to "telephone",
                    ), timestamp = nowAsEpochMilliseconds()
                )
            )
        )
    }

    @Test fun `isApplicableTo returns true for known places with old amenity=telephone`() {
        val milisecondsFor800Days: Long = 1000L * 60 * 60 * 24 * 800
        assertTrue(
            questType.isApplicableTo(
                node(
                    tags = mapOf(
                        "amenity" to "telephone",
                    ), timestamp = nowAsEpochMilliseconds() - milisecondsFor800Days
                )
            )
        )
    }
}
