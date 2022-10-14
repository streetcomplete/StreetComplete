package de.westnordost.streetcomplete.data.urlconfig

import de.westnordost.streetcomplete.data.overlay.TestOverlayA
import de.westnordost.streetcomplete.data.overlay.TestOverlayB
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.quest.TestQuestTypeB
import de.westnordost.streetcomplete.data.quest.TestQuestTypeC
import de.westnordost.streetcomplete.data.quest.TestQuestTypeD
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class UrlConfigKtTest {

    private val q0 = TestQuestTypeA()
    private val q1 = TestQuestTypeB()
    private val q2 = TestQuestTypeC()
    private val q3 = TestQuestTypeD()

    private val o0 = TestOverlayA()
    private val o1 = TestOverlayB()

    private val quests = QuestTypeRegistry(listOf(0 to q0, 1 to q1, 2 to q2, 3 to q3))

    private val overlays = OverlayRegistry(listOf(0 to o0, 1 to o1))

    //region parse url

    @Test fun `parse simple config`() {
        assertEquals(
            UrlConfig("Test", listOf(q0, q2, q3), null),
            parseConfigUrl("https://streetcomplete.io/?n=Test&q=13", quests, overlays)
        )
    }

    @Test fun `parse config with overlay`() {
        assertEquals(
            UrlConfig("Test", listOf(q0, q2, q3), o1),
            parseConfigUrl("https://streetcomplete.io/?n=Test&q=13&o=1", quests, overlays)
        )
    }

    @Test fun `upper case is fine`() {
        assertEquals(
            UrlConfig("Test", listOf(q0, q2, q3), null),
            parseConfigUrl("HTTPS://STREETCOMPLETE.IO/?N=Test&Q=13", quests, overlays)
        )
    }

    @Test fun `url decode name`() {
        assertEquals(
            UrlConfig("Hello Wörld", listOf(q0), null),
            parseConfigUrl("https://streetcomplete.io/?n=Hello+W%C3%B6rld&q=1", quests, overlays)
        )
    }

    @Test fun `reject wrong domain`() {
        assertNull(parseConfigUrl("https://blorb.io/?n=Test&q=13", quests, overlays))
    }

    @Test fun `reject misformed parameters`() {
        assertNull(parseConfigUrl("https://streetcomplete.io/?n=Test=Wrong&q=13", quests, overlays))
        assertNull(parseConfigUrl("https://streetcomplete.io/?n=Test&s&q=13", quests, overlays))
        assertNull(parseConfigUrl("https://streetcomplete.io/?n=Test&&q=13", quests, overlays))
    }

    @Test fun `reject if name is missing`() {
        assertNull(parseConfigUrl("https://streetcomplete.io/?q=13", quests, overlays))
    }

    @Test fun `reject if quests is missing`() {
        assertNull(parseConfigUrl("https://streetcomplete.io/?q=13", quests, overlays))
    }

    //endregion

    //region create url

    @Test fun `create simple url`() {
        assertEquals(
            "https://streetcomplete.io/?n=Test&q=13",
            createConfigUrl(UrlConfig("Test", listOf(q0, q2, q3), null), quests, overlays),
        )
    }

    @Test fun `create url with overlay`() {
        assertEquals(
            "https://streetcomplete.io/?n=Test&q=13&o=1",
            createConfigUrl(UrlConfig("Test", listOf(q0, q2, q3), o1), quests, overlays),
        )
    }

    @Test fun `url encode name`() {
        assertEquals(
            "https://streetcomplete.io/?n=Hello+W%C3%B6rld&q=1",
            createConfigUrl(UrlConfig("Hello Wörld", listOf(q0), null), quests, overlays)
        )
    }

    //endregion
}
