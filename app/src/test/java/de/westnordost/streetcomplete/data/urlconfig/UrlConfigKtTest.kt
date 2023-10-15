package de.westnordost.streetcomplete.data.urlconfig

import de.westnordost.streetcomplete.data.overlay.TestOverlayA
import de.westnordost.streetcomplete.data.overlay.TestOverlayB
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.quest.TestQuestTypeB
import de.westnordost.streetcomplete.data.quest.TestQuestTypeC
import de.westnordost.streetcomplete.data.quest.TestQuestTypeD
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class UrlConfigKtTest {

    private val q0 = TestQuestTypeA()
    private val q1 = TestQuestTypeB()
    private val q2 = TestQuestTypeC()
    private val q3 = TestQuestTypeD()

    private val qUnknown = TestQuestTypeD()

    private val o0 = TestOverlayA()
    private val o1 = TestOverlayB()

    private val quests = QuestTypeRegistry(listOf(0 to q0, 1 to q1, 2 to q2, 3 to q3))

    private val overlays = OverlayRegistry(listOf(0 to o0, 1 to o1))

    //region parse url

    @Test fun `parse config without name`() {
        assertEquals(
            UrlConfig(null, listOf(q0, q2, q3), emptyList(), null),
            parseConfigUrl("https://streetcomplete.app/s?q=d", quests, overlays)
        )
    }

    @Test fun `parse simple config`() {
        assertEquals(
            UrlConfig("Test", listOf(q0, q2, q3), emptyList(), null),
            parseConfigUrl("https://streetcomplete.app/s?n=Test&q=d", quests, overlays)
        )
    }

    @Test fun `parse config using custom url scheme`() {
        assertEquals(
            UrlConfig("Test", listOf(q0, q2, q3), emptyList(), null),
            parseConfigUrl("streetcomplete://s?n=Test&q=d", quests, overlays)
        )
    }

    @Test fun `parse config with overlay`() {
        assertEquals(
            UrlConfig("Test", listOf(q0, q2, q3), emptyList(), o1),
            parseConfigUrl("https://streetcomplete.app/s?n=Test&q=d&o=1", quests, overlays)
        )
    }

    @Test fun `upper case is fine`() {
        assertEquals(
            UrlConfig("Test", listOf(q0, q2, q3), emptyList(), null),
            parseConfigUrl("https://streetcomplete.app/s?N=Test&Q=D", quests, overlays)
        )
    }

    @Test fun `url decode name`() {
        assertEquals(
            UrlConfig("Hello Wörld", listOf(q0), emptyList(), null),
            parseConfigUrl("https://streetcomplete.app/s?n=Hello+W%C3%B6rld&q=1", quests, overlays)
        )
    }

    @Test fun `parse config with quest sort orders`() {
        assertEquals(
            UrlConfig("Test", listOf(q0, q2, q3), listOf(q0 to q1, q3 to q0), null),
            parseConfigUrl("https://streetcomplete.app/s?n=Test&q=d&qo=0.1-3.0", quests, overlays)
        )
    }

    @Test fun `parse ignores unknown ordinals`() {
        assertEquals(
            UrlConfig("Test", listOf(q0, q2, q3), listOf(q1 to q3), null),
            // i.e. ordinal 4 is ignored
            parseConfigUrl("https://streetcomplete.app/s?n=Test&q=t&qo=0.4-1.3", quests, overlays)
        )
    }

    @Test fun `reject wrong domain`() {
        assertNull(parseConfigUrl("https://blorb.io/?n=Test&q=d", quests, overlays))
    }

    @Test fun `reject malformed parameters`() {
        assertNull(parseConfigUrl("https://streetcomplete.app/s?n=Test=Wrong&q=d", quests, overlays))
        assertNull(parseConfigUrl("https://streetcomplete.app/s?n==Wrong&q=d", quests, overlays))
        assertNull(parseConfigUrl("https://streetcomplete.app/s?n=Test&qo&q=d", quests, overlays))
        assertNull(parseConfigUrl("https://streetcomplete.app/s?n=Test&&q=d", quests, overlays))

        assertNull(parseConfigUrl("https://streetcomplete.app/s?n=Test&q=d&qo=0.1--1.2", quests, overlays))
        assertNull(parseConfigUrl("https://streetcomplete.app/s?n=Test&q=d&qo=0.1-1.2-", quests, overlays))
        assertNull(parseConfigUrl("https://streetcomplete.app/s?n=Test&q=d&qo=0", quests, overlays))
        assertNull(parseConfigUrl("https://streetcomplete.app/s?n=Test&q=d&qo=0..1", quests, overlays))
        assertNull(parseConfigUrl("https://streetcomplete.app/s?n=Test&q=d&qo=0.0", quests, overlays))
    }

    @Test fun `reject if quests is missing`() {
        assertNull(parseConfigUrl("https://streetcomplete.app/s?n=d", quests, overlays))
    }

    //endregion

    //region create url

    @Test fun `create url without name`() {
        assertEquals(
            "https://streetcomplete.app/s?q=d",
            createConfigUrl(UrlConfig(null, listOf(q0, q2, q3), emptyList(), null), quests, overlays),
        )
    }

    @Test fun `create simple url`() {
        assertEquals(
            "https://streetcomplete.app/s?n=Test&q=d",
            createConfigUrl(UrlConfig("Test", listOf(q0, q2, q3), emptyList(), null), quests, overlays),
        )
    }

    @Test fun `create url with overlay`() {
        assertEquals(
            "https://streetcomplete.app/s?n=Test&q=d&o=1",
            createConfigUrl(UrlConfig("Test", listOf(q0, q2, q3), emptyList(), o1), quests, overlays),
        )
    }

    @Test fun `create url with quest type orders`() {
        assertEquals(
            "https://streetcomplete.app/s?n=Test&q=d&qo=0.1-3.2",
            createConfigUrl(UrlConfig("Test", listOf(q0, q2, q3), listOf(q0 to q1, q3 to q2), null), quests, overlays),
        )
    }

    @Test fun `url encode name`() {
        assertEquals(
            "https://streetcomplete.app/s?n=Hello+W%C3%B6rld&q=1",
            createConfigUrl(UrlConfig("Hello Wörld", listOf(q0), emptyList(), null), quests, overlays)
        )
    }

    @Test fun `shorten name if too long`() {
        assertEquals(
            "https://streetcomplete.app/s?n=123456789012345678901234567890123456789012345678901234567...&q=1",
            createConfigUrl(UrlConfig("123456789012345678901234567890123456789012345678901234567890X", listOf(q0), emptyList(), null), quests, overlays)
        )
    }

    @Test fun `create url ignores unknown quests`() {
        assertEquals(
            "https://streetcomplete.app/s?n=Test&q=1&qo=3.2",
            createConfigUrl(UrlConfig("Test", listOf(q0, qUnknown), listOf(q0 to qUnknown, q3 to q2), null), quests, overlays),
        )
    }

    //endregion
}
