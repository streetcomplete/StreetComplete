package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import kotlin.test.Test
import kotlin.test.assertEquals

class ChangesetApiSerializerTest {

    @Test fun `serialize to xml`() {
        val osm = """
            <osm>
            <changeset>
            <tag k="one" v="1" />
            <tag k="two" v="2" />
            </changeset>
            </osm>
        """

        val changesetTags = mapOf(
            "one" to "1",
            "two" to "2",
        )

        assertEquals(
            osm.replace(Regex("[\n\r] *"), ""),
            ChangesetApiSerializer().serialize(changesetTags)
        )
    }
}
