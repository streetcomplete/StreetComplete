package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import java.util.concurrent.FutureTask
import kotlin.test.assertEquals
import kotlin.test.Test

class CheckShopExistenceTest {
    private val questType = CheckShopExistence(mockOfFeatureDictionary())

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
        on(tagFinder.byTags(mapOf("shop" to "greengrocer", "name" to "Foobar"))).thenReturn(matchFound)
        on(tagFinder.byTags(mapOf("shop" to "weird_value", "name" to "Foobar"))).thenReturn(noMatches)
        on(tagFinder.byTags(mapOf("shop" to "weird_value"))).thenReturn(noMatches)

        val tagFinderFutureTask: FutureTask<FeatureDictionary> = mock()
        on(tagFinderFutureTask.get()).thenReturn(tagFinder)
        return tagFinderFutureTask
    }

    @Test
    fun `not applicable to old shops with unrecognised values and without name`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(timestamp = 0, tags = mapOf("shop" to "weird_value")),
            ),
        )
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `applicable to old shops with unrecognised values and with name`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(timestamp = 0, tags = mapOf("shop" to "weird_value", "name" to "Foobar")),
            ),
        )
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `applicable to old greengrocer shops`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(timestamp = 0, tags = mapOf("shop" to "greengrocer", "name" to "Foobar")),
            ),
        )
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }
}
