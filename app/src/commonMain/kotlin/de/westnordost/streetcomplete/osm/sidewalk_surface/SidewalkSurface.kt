package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.surface.Surface
import kotlin.jvm.JvmInline

@JvmInline
value class SidewalkSurface(val value: Sides<Surface>)
