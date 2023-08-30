package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.osm.MAXSPEED_TYPE_KEYS

val anyMaxSpeedTagKey = "~${(MAXSPEED_TYPE_KEYS + "maxspeed").joinToString("|")}"

val onlyInANoneMaxSpeed30OrLessZone = """
  (
    $anyMaxSpeedTagKey !~ ".*:(zone)?:?([1-9]|[1-2][0-9]|30)"
    and $anyMaxSpeedTagKey ~ ".*:(urban|rural|trunk|motorway|nsl_single|nsl_dual)"
  )
"""
