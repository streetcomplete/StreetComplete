package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.resources.*

class DisabledTestQuestType : TestQuestType() {
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside
}
