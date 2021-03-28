package de.westnordost.streetcomplete.data.maptiles

import android.util.Log
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.Downloader
import de.westnordost.streetcomplete.map.VectorTileProvider
import de.westnordost.streetcomplete.util.TilesRect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.internal.Version
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MapTilesDownloader @Inject constructor(
    private val vectorTileProvider: VectorTileProvider,
    private val cacheConfig: MapTilesDownloadCacheConfig
) : Downloader, CoroutineScope by CoroutineScope(Dispatchers.IO) {

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

    private fun downloadTiles(tiles: TilesRect, cancelState: AtomicBoolean) = runBlocking {
        var tileCount = 0
        var failureCount = 0
        var downloadedSize = 0
        var cachedSize = 0
        val time = System.currentTimeMillis()
        for (zoom in vectorTileProvider.maxZoom downTo 0) {
            if (!cancelState.get()) {
                val tilesAtZoom = tiles.zoom(ApplicationConstants.QUEST_TILE_ZOOM, zoom)
                for (tile in tilesAtZoom.asTileSequence()) {
                    launch {
                        if (!cancelState.get()) {
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
                }
            }
        }
        val seconds = (System.currentTimeMillis() - time) / 1000
        val failureText = if (failureCount > 0) ". $failureCount tiles failed to download" else ""
        Log.i(TAG, "Fetched $tileCount tiles (${downloadedSize / 1000}kB downloaded, ${cachedSize / 1000}kB already cached) in ${seconds}s$failureText")
    }

    private suspend fun downloadTile(zoom: Int, x: Int, y: Int): DownloadResult = suspendCoroutine { cont ->
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

        /* since we use coroutines and this is in the background anyway, why not use call.execute()?
        *  Because we want to let the OkHttp dispatcher control how many HTTP requests are made in
        *  parallel */
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
        call.enqueue(callback)
    }

    companion object {
        private const val TAG = "MapTilesDownload"
    }
}

private sealed class DownloadResult
private data class DownloadSuccess(val alreadyCached: Boolean, val size: Int) : DownloadResult()
private object DownloadFailure : DownloadResult()
