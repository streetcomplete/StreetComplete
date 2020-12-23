package de.westnordost.streetcomplete.data.maptiles

import android.util.Log
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.Downloader
import de.westnordost.streetcomplete.map.VectorTileProvider
import de.westnordost.streetcomplete.util.TilesRect
import okhttp3.*
import okhttp3.internal.Version
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class MapTilesDownloader @Inject constructor(
    private val vectorTileProvider: VectorTileProvider,
    private val cacheConfig: MapTilesDownloadCacheConfig
) : Downloader {

    private val okHttpClient = OkHttpClient.Builder().cache(cacheConfig.cache).build()

    override fun download(tiles: TilesRect, cancelState: AtomicBoolean) {
        if (cancelState.get()) return

        Log.i(TAG, "(${tiles.getAsLeftBottomRightTopString()}) Starting")

        try {
            downloadTiles(tiles, cancelState)
        } finally {
            Log.i(TAG, "(${tiles.getAsLeftBottomRightTopString()}) Finished")
        }
    }

    private fun downloadTiles(tiles: TilesRect, cancelState: AtomicBoolean) {
        var tileCount = 0
        var failureCount = 0
        var downloadedSize = 0
        var cachedSize = 0
        val time = System.currentTimeMillis()
        // through all zoom levels, most important first...
        for (zoom in vectorTileProvider.maxZoom downTo 0) {
            if (cancelState.get()) return
            val tilesAtZoom = tiles.zoom(ApplicationConstants.QUEST_TILE_ZOOM, zoom)
            // all tiles in tile rect in that zoom level...
            for (tile in tilesAtZoom.asTileSequence()) {
                if (cancelState.get()) return
                val result = downloadTile(zoom, tile.x, tile.y)
                ++tileCount
                when (result) {
                    is DownloadFailure -> ++failureCount
                    is DownloadSuccess -> {
                        if (result.alreadyCached) cachedSize += result.size
                        else downloadedSize += result.size
                    }
                }
            }
        }
        val seconds = (System.currentTimeMillis() - time) / 1000
        val failureText = if (failureCount > 0) ". $failureCount tiles failed to download" else ""
        Log.i(TAG, "Fetched $tileCount tiles (${downloadedSize / 1000}kB downloaded, ${cachedSize / 1000}kB already cached) in ${seconds}s$failureText")
    }

    private fun downloadTile(zoom: Int, x: Int, y: Int): DownloadResult {
        /* adding trailing "&" because Tangram-ES also puts this at the end and the URL needs to be
           identical in order for the cache to work */
        val url = vectorTileProvider.getTileUrl(zoom, x, y) + "&"
        val httpUrl = HttpUrl.parse(url)
        check(httpUrl != null) { "Invalid URL: $url" }

        val builder = Request.Builder()
            .url(httpUrl)
            .cacheControl(cacheConfig.cacheControl)
        builder.header("User-Agent", ApplicationConstants.USER_AGENT + " / " + Version.userAgent())
        val call = okHttpClient.newCall(builder.build())

        try {
            val response = call.execute()
            var size = 0
            response.body()?.use { body ->
                // just get the bytes and let the cache magic do the rest...
                size = body.bytes().size
            }
            val alreadyCached = response.cacheResponse() != null
            val logText = if (alreadyCached) "in cache" else "downloaded"
            Log.v(TAG, "Tile $zoom/$x/$y $logText")
            return DownloadSuccess(alreadyCached, size)
        } catch (e: IOException) {
            Log.w(TAG, "Error retrieving tile $zoom/$x/$y: ${e.message}")
            return DownloadFailure
        }
    }

    companion object {
        private const val TAG = "MapTilesDownload"
    }
}

private sealed class DownloadResult
private data class DownloadSuccess(val alreadyCached: Boolean, val size: Int) : DownloadResult()
private object DownloadFailure : DownloadResult()
