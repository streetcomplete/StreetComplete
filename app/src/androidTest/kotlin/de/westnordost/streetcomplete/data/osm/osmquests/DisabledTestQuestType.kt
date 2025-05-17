package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.R

class DisabledTestQuestType : TestQuestType() {
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside
}
