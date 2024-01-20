package de.westnordost.streetcomplete.data.maptiles

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.screens.main.map.VectorTileProvider
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.Version
import java.io.IOException
import kotlin.coroutines.resume

class MapTilesDownloader(
    private val vectorTileProvider: VectorTileProvider,
    private val cacheConfig: MapTilesDownloadCacheConfig
) {
    private val okHttpClient = OkHttpClient.Builder().cache(cacheConfig.cache).build()

    data class Tile(val zoom: Int, val x: Int, val y: Int)

    suspend fun download(bbox: BoundingBox) = withContext(Dispatchers.IO) {
        var tileCount = 0
        var failureCount = 0
        var downloadedSize = 0
        var cachedSize = 0
        val time = nowAsEpochMilliseconds()

        coroutineScope {
            for (tile in getDownloadTileSequence(bbox)) {
                launch {
                    val result = try {
                        downloadTile(tile.zoom, tile.x, tile.y)
                    } catch (e: Exception) {
                        DownloadFailure
                    }
                    ++tileCount
                    when (result) {
                        is DownloadFailure -> ++failureCount
                        is DownloadSuccess -> {
                            if (result.alreadyCached) {
                                cachedSize += result.size
                            } else {
                                downloadedSize += result.size
                            }
                        }
                    }
                }
            }
        }
        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
        val failureText = if (failureCount > 0) ". $failureCount tiles failed to download" else ""
        Log.i(TAG, "Downloaded $tileCount tiles (${downloadedSize / 1000}kB downloaded, ${cachedSize / 1000}kB already cached) in ${seconds.format(1)}s$failureText")
    }

    private fun getDownloadTileSequence(bbox: BoundingBox): Sequence<Tile> =
        /* tiles for the highest zoom (=likely current or near current zoom) first,
           because those are the tiles that are likely to be useful first */
        (vectorTileProvider.maxZoom downTo 0).asSequence().flatMap { zoom ->
            bbox.enclosingTilesRect(zoom).asTilePosSequence().map { Tile(zoom, it.x, it.y) }
        }

    private suspend fun downloadTile(zoom: Int, x: Int, y: Int): DownloadResult = suspendCancellableCoroutine { cont ->
        /* adding trailing "&" because Tangram-ES also puts this at the end and the URL needs to be
           identical in order for the cache to work */
        val url = vectorTileProvider.getTileUrl(zoom, x, y) + "&"
        val httpUrl = HttpUrl.parse(url)
        require(httpUrl != null) { "Invalid URL: $url" }

        val builder = Request.Builder()
            .url(httpUrl)
            .cacheControl(cacheConfig.cacheControl)
        builder.header("User-Agent", ApplicationConstants.USER_AGENT + " / " + Version.userAgent())
        val call = okHttpClient.newCall(builder.build())

        /* since we use coroutines and this is in the background anyway, why not use call.execute()?
         * Because we want to let the OkHttp dispatcher control how many HTTP requests are made in
         * parallel */
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.w(TAG, "Error retrieving tile $zoom/$x/$y: ${e.message}")
                cont.resume(DownloadFailure)
            }

            override fun onResponse(call: Call, response: Response) {
                var size = 0
                response.body()?.use { body ->
                    // just get the bytes and let the cache magic do the rest...
                    size = body.bytes().size
                }
                val alreadyCached = response.cacheResponse() != null
                val logText = if (alreadyCached) "in cache" else "downloaded"
                Log.v(TAG, "Tile $zoom/$x/$y $logText")
                cont.resume(DownloadSuccess(alreadyCached, size))
            }
        }
        cont.invokeOnCancellation { call.cancel() }
        call.enqueue(callback)
    }

    companion object {
        private const val TAG = "MapTilesDownload"
    }
}

private sealed class DownloadResult
private data class DownloadSuccess(val alreadyCached: Boolean, val size: Int) : DownloadResult()
private object DownloadFailure : DownloadResult()
