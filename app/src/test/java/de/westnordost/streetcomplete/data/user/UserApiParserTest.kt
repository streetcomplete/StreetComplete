package de.westnordost.streetcomplete.data.user

import kotlin.test.Test
import kotlin.test.assertEquals

class UserApiParserTest {

    @Test
    fun `parse minimum user info`() {
        val xml = """<osm><user display_name="Max Muster" id="1234"/></osm>""".trimIndent()

        assertEquals(
            UserInfo(
                id = 1234,
                displayName = "Max Muster",
                profileImageUrl = null
            ),
            UserApiParser().parseUsers(xml).single()
        )
    }

    @Test
    fun `parse full user info`() {
        val xml = """
            <osm version="0.6" generator="OpenStreetMap server" copyright="OpenStreetMap and contributors" attribution="http://www.openstreetmap.org/copyright" license="http://opendatacommons.org/licenses/odbl/1-0/">
              <user display_name="Max Muster" account_created="2006-07-21T19:28:26Z" id="1234">
                <contributor-terms agreed="true" pd="true"/>
                <img href="https://www.openstreetmap.org/attachments/users/images/000/000/1234/original/someLongURLOrOther.JPG"/>
                <roles></roles>
                <changesets count="4182"/>
                <traces count="513"/>
                <blocks>
                  <received count="0" active="0"/>
                </blocks>
                <home lat="49.4733718952806" lon="8.89285988577866" zoom="3"/>
                <description>The description of your profile</description>
                <languages>
                  <lang>de-DE</lang>
                  <lang>de</lang>
                  <lang>en-US</lang>
                  <lang>en</lang>
                </languages>
                <messages>
                  <received count="1" unread="0"/>
                  <sent count="0"/>
                </messages>
              </user>
            </osm>
        """.trimIndent()

        assertEquals(
            UserInfo(
                id = 1234,
                displayName = "Max Muster",
                profileImageUrl = "https://www.openstreetmap.org/attachments/users/images/000/000/1234/original/someLongURLOrOther.JPG",
                unreadMessagesCount = 0,
            ),
            UserApiParser().parseUsers(xml).single()
        )
    }
}
