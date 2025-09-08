package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.default_disabled_msg_go_inside

class DisabledTestQuestType : TestQuestType() {
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside
}
