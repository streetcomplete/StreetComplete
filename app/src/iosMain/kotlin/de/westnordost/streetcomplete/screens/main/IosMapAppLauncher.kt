package de.westnordost.streetcomplete.screens.main

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import platform.Foundation.NSURL
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleActionSheet
import platform.UIKit.UIApplication

object IosMapAppLauncher : MapAppLauncher {
    override fun openAt(position: LatLon, zoom: Double) {
        val app = UIApplication.sharedApplication

        val alert = UIAlertController.alertControllerWithTitle(
            title = null,
            message = null,
            preferredStyle = UIAlertControllerStyleActionSheet
        )

        for (mapApp in mapApps) {
            val url = NSURL.URLWithString(mapApp.buildUrl(position.latitude, position.longitude, zoom))!!
            if (app.canOpenURL(url)) {
                alert.addAction(UIAlertAction.actionWithTitle(mapApp.title, UIAlertActionStyleDefault) {
                    app.openURL(url, options = emptyMap<Any?, Any?>(), completionHandler = null)
                })
            }
        }

        val rootViewController = app.keyWindow?.rootViewController
        rootViewController?.presentViewController(alert, animated = true, completion = null)
    }

    override fun isAvailable(): Boolean {
        val app = UIApplication.sharedApplication
        return mapApps.any { mapApp ->
            val url = NSURL.URLWithString(mapApp.buildUrl(0.0, 0.0, 18.0))!!
            app.canOpenURL(url)
        }
    }

    private data class MapApp(
        val title: String,
        val buildUrl: (lat: Double, lon: Double, zoom: Double) -> String
    )

    /*
     * Since iOS does not support that multiple apps register for the same URI scheme, i.e. in this
     * case the geo: URI scheme but instead the "last-installed app wins", we have to maintain a
     * list of apps to link to manually that register an own scheme.
     * This is only possible for apps that *do* offer a custom URI scheme.
     *
     * Inclusion policy:
     *
     * Since the purpose of the "open location in another app" function is to primarily link to
     * another (full) OSM editor, everything except editors are just courtesy and thus only included
     * if they are open source and based on openstreetmap.
     * Apple Maps is included because it is the default map app on iOS.
     *  */
    private val mapApps = listOf(
        MapApp(
            title = "Go Map!!",
            // see https://github.com/bryceco/GoMap/blob/d52d52ed9ae2ba6a44c859fc559e0e2513136341/src/Shared/LocationParser.swift#L219
            buildUrl = { lat, lon, zoom -> "gomaposm://?center=$lat,$lon&zoom=$zoom" }
        ),
        MapApp(
            title = "Apple Maps",
            buildUrl = { lat, lon, zoom -> "http://maps.apple.com/?ll=$lat,$lon" }
        ),
        MapApp(
            title = "OsmAnd",
            // see https://github.com/osmandapp/OsmAnd-iOS/blob/2e6202f7cd7114aee532f77a8ab277f9bd1c2e62/Sources/Helpers/DeepLinkManager/DeepLinkParser.swift#L364
            buildUrl = { lat, lon, zoom -> "osmandmaps://?lat=$lat&lon=$lon&z=$zoom" }
        ),
        MapApp(
            title = "CoMaps",
            // see https://codeberg.org/comaps/comaps/src/branch/main/libs/map/mwm_url.cpp
            buildUrl = { lat, lon, zoom -> "cm://map?ll=$lat,$lon&z=$zoom" }
        ),
        MapApp(
            title = "Organic Maps",
            // see https://github.com/organicmaps/organicmaps/blob/master/libs/map/mwm_url.cpp
            buildUrl = { lat, lon, zoom -> "om://map?ll=$lat,$lon&z=$zoom" }
        )
    )
}
