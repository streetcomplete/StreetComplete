package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertEquals

class AddPostboxRoyalCypherTest {

    private val questType = AddPostboxRoyalCypher()

    @Test fun `not applicable to postboxes outside GB`() {
        // FR (France)
        assertEquals(true, questType.isApplicableTo(
            node(tags = mapOf("amenity" to "post_box"), pos = LatLon(48.728532, 2.369866))
        ))
    }

    @Test fun `not applicable to postboxes in GB that already have a royal cypher mapped`() {
        assertEquals(false, questType.isApplicableTo(
            node(tags = mapOf("amenity" to "post_box", "royal_cypher" to "EIIR"), pos = LatLon(51.651735,-0.149748))
        ))
    }

    @Test fun `applicable to postboxes in GB, Crown Dependencies and Overseas Territories that not already have a royal cypher mapped`() {
        // GB-ENG (England)
        assertEquals(true, questType.isApplicableTo(
            node(tags = mapOf("amenity" to "post_box"), pos = LatLon(51.651735,-0.149748))
        ))
        // IM (Isle of Men - implicit via GB)
        assertEquals(true, questType.isApplicableTo(
            node(tags = mapOf("amenity" to "post_box"), pos = LatLon(54.149545,-4.482395))
        ))
        // GG (Guernsey - implicit via GB)
        assertEquals(true, questType.isApplicableTo(
            node(tags = mapOf("amenity" to "post_box"), pos = LatLon(49.457148,-2.537177))
        ))
        // JE (Jersey - implicit via GB)
        assertEquals(true, questType.isApplicableTo(
            node(tags = mapOf("amenity" to "post_box"), pos = LatLon(49.185224,-2.109332))
        ))
        // BM (Bermuda - implicit via GB)
        assertEquals(true, questType.isApplicableTo(
            node(tags = mapOf("amenity" to "post_box"), pos = LatLon(32.294316,-64.778953))
        ))
        // KY (Cayman Islands - implicit via GB)
        assertEquals(true, questType.isApplicableTo(
            node(tags = mapOf("amenity" to "post_box"), pos = LatLon(19.273124,-81.298040))
        ))
        // CY (Cyprus - explicit)
        assertEquals(true, questType.isApplicableTo(
            node(tags = mapOf("amenity" to "post_box"), pos = LatLon(35.180561,33.361568))
        ))
        // GI (Gibraltar - explicit)
        assertEquals(true, questType.isApplicableTo(
            node(tags = mapOf("amenity" to "post_box"), pos = LatLon(36.143918,-5.358498))
        ))
        // MT (Malta - explicit)
        assertEquals(true, questType.isApplicableTo(
            node(tags = mapOf("amenity" to "post_box"), pos = LatLon(35.873912,14.504346))
        ))
        // HK (Hong Kong - explicit)
        assertEquals(true, questType.isApplicableTo(
            node(tags = mapOf("amenity" to "post_box"), pos = LatLon(22.339081,114.185334))
        ))
    }

    @Test fun `apply no cypher answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("royal_cypher", "no")),
            questType.answerApplied(PostboxRoyalCypher.NONE)
        )
    }

    @Test fun `apply Charles III answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("royal_cypher", "CIIIR")),
            questType.answerApplied(PostboxRoyalCypher.CHARLES_III)
        )
    }
}
