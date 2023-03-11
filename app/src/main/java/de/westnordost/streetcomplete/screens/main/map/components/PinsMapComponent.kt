package de.westnordost.streetcomplete.screens.main.map.components

import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.PropertyValue
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapzen.tangram.MapData
import com.mapzen.tangram.geometry.Point
import de.westnordost.streetcomplete.data.maptiles.toLatLng
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.MainActivity
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.toLngLat

/** Takes care of displaying pins on the map, e.g. quest pins or pins for recent edits */
class PinsMapComponent(ctrl: KtMapController) {

    private val pinsLayer: MapData = ctrl.addDataLayer(PINS_LAYER)

    /** Shows/hides the pins */
    var isVisible: Boolean
        get() = pinsLayer.visible
        set(value) { pinsLayer.visible = value }

    /** Show given pins. Previously shown pins are replaced with these.  */
    fun set(pins: Collection<Pin>) {
        pinsLayer.setFeatures(pins.map { pin ->
            // avoid creation of intermediate HashMaps.
            val tangramProperties = listOf(
                "type" to "point",
                "kind" to pin.iconName,
                "importance" to pin.importance.toString()
            )
            val properties = HashMap<String, String>()
            properties.putAll(tangramProperties)
            properties.putAll(pin.properties)
            Point(pin.position.toLngLat(), properties)
        })

        // todo: crash if not on UI thread
        //  for now the runOnUiThread is ok, but actually it should be handled differently...
        MainActivity.activity?.runOnUiThread {

            // using pinsSource (currently image per pin does not work)
            MainMapFragment.pinsSource?.setGeoJson(FeatureCollection.fromFeatures(pins.map {
                Feature.fromJson(
                """
                {
                type: Feature,
                geometry:{type=Point, coordinates=[${it.position.longitude}, ${it.position.latitude}]},
                 properties={"symbol-sort-key":-${it.importance.toFloat()},
                 "icon-size":0.3,
                 "icon-image":"${it.iconName}",
                 "id":0,
                 "custom_data":{"quest_group":"osm","element_type":"WAY","element_id":"161885673","quest_type":"AddBuildingType"}
                 },
                  isDraggable=false}
                """.trimIndent()
//                 {geometry=Point{type=Point, bbox=null, coordinates=[${it.position.longitude}, ${it.position.latitude}]}, properties={"symbol-sort-key":-${it.importance.toFloat()},"icon-size":0.3,"icon-image":"${it.iconName}","icon-rotate":null,"icon-offset":null,"icon-anchor":null,"text-field":null,"text-font":null,"text-size":null,"text-max-width":null,"text-letter-spacing":null,"text-justify":null,"text-radial-offset":null,"text-anchor":null,"text-rotate":null,"text-transform":null,"text-offset":null,"icon-opacity":null,"icon-color":null,"icon-halo-color":null,"icon-halo-width":null,"icon-halo-blur":null,"text-opacity":null,"text-color":null,"text-halo-color":null,"text-halo-width":null,"text-halo-blur":null,"id":0,"custom_data":{"quest_group":"osm","element_type":"WAY","element_id":"161885673","quest_type":"AddBuildingType"}}, isDraggable=false}
                )
                val p = JsonObject()
                p.addProperty("icon-image", it.iconName)
                p.addProperty("icon-size", 0.3f)
                Feature.fromGeometry(com.mapbox.geojson.Point.fromLngLat(it.position.longitude, it.position.latitude), p)
            })) // todo: not shown on the map... what's wrong?
                //  probably some properties, because it's shown in a circleLayer
                //  hmm... i can set the same properties for the layer, but not for a single feature -> ??

            // using annotationmanagers (too slow)
/*            MainMapFragment.pinSymbolManager!!.deleteAll()
            // todo: symbolManager returns symbols created from the pins, which can be used to delete single symbols from the map
            //  is the order of the returned list the same as the symbol options list?
            //  this is important for associating symbols with quests (there might be performance issues when adding many pins one by one)
            //  -> yes according to https://github.com/maplibre/maplibre-plugins-android/blob/main/plugin-annotation/src/main/java/com/mapbox/mapboxsdk/plugins/annotation/AnnotationManager.java#L150
            val symbols = MainMapFragment.pinSymbolManager!!.create(pins.map { pin ->
                SymbolOptions() // actually creating should not be done on UI thread (but later...)
                    .withLatLng(pin.position.toLatLng())
                    .withIconImage(pin.iconName)
                    .withIconSize(0.3f) // seems smaller than tangram, maybe depends on pixel ratio
                    .withData(pin.jsonProps)
                    .withSymbolSortKey(-pin.importance.toFloat())
            })
            Log.i("test", symbols.first().toString())
            MainMapFragment.pinSymbolManager!!.deleteAll()
            val p = JsonObject()
            p.addProperty("icon-image", "bla")
            p.addProperty("icon-size", 0.3f)
            Log.i("test", p.entrySet().toString())*/
/*            MainMapFragment.pinDotManager!!.deleteAll()
            MainMapFragment.pinDotManager!!.create(pins.map { pin ->
                CircleOptions()
                    // doesn't look good, maybe better use an icon
                    .withLatLng(pin.position.toLatLng())
                    .withCircleColor("white")
                    .withCircleStrokeColor("black")
                    .withCircleRadius(5f)
                    .withCircleStrokeWidth(1f)
            })*/
        }
    }

    /** Clear pins */
    fun clear() {
        pinsLayer.clear()
    }

    companion object {
        // see streetcomplete.yaml for the definitions of the below layers
        private const val PINS_LAYER = "streetcomplete_pins"
    }
}

data class Pin(
    val position: LatLon,
    val iconName: String,
    val properties: Collection<Pair<String, String>> = emptyList(),
    val jsonProps: JsonElement,
    val importance: Int = 0
)
