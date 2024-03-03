package de.westnordost.streetcomplete.screens.main.map.maplibre

import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.*

/* expression for line width dependent on zoom (line width in property in meters) */
fun changeDistanceWithZoom(propertyName: String): Expression =
    interpolate(exponential(2), zoom(),
        stop(10, division(get(propertyName), literal(128))),
        stop(24, product(get(propertyName), literal(128)))
    )
