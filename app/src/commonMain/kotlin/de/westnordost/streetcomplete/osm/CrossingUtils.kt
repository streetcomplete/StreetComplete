package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element

private val isCrossingExpr by lazy { """
    nodes with
      highway = traffic_signals and crossing = traffic_signals
      or highway = crossing
""".toElementFilterExpression() }

private val isCrossingWithTrafficSignalsExpr by lazy { """
    nodes with
      highway ~ crossing|traffic_signals
      and (
        crossing = traffic_signals
        or crossing:signals and crossing:signals != no
      )
""".toElementFilterExpression() }

fun Element.isCrossing(): Boolean = isCrossingExpr.matches(this)

fun Element.isCrossingWithTrafficSignals(): Boolean = isCrossingWithTrafficSignalsExpr.matches(this)
