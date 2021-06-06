package de.westnordost.streetcomplete.data.maptiles

import android.content.Context
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.ApplicationConstants.DELETE_OLD_DATA_AFTER
import de.westnordost.streetcomplete.Prefs
import okhttp3.*
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Configuration for the common cache shared by tangram-es and the map tile ("pre"-)downloader
 *  integrated into the normal map download process */
@Singleton class MapTilesDownloadCacheConfig @Inject constructor(context: Context) {

    val cacheControl = CacheControl.Builder()
        .maxAge(12, TimeUnit.HOURS)
        .maxStale(DELETE_OLD_DATA_AFTER.toInt(), TimeUnit.MILLISECONDS)
        .build()

    val cache: Cache?

    init {
        val cacheDir = context.externalCacheDir
        val tileCacheDir: File?
        if (cacheDir != null) {
            tileCacheDir = File(cacheDir, TILE_CACHE_DIR)
            if (!tileCacheDir.exists()) tileCacheDir.mkdir()
        } else {
            tileCacheDir = null
        }

        cache = if (tileCacheDir?.exists() == true) {
            val mbs = PreferenceManager.getDefaultSharedPreferences(context).getInt(Prefs.MAP_TILECACHE_IN_MB, 50)
            Cache(tileCacheDir, mbs * 1000L * 1000L)
        } else {
            null
        }
    }

    companion object {
        private const val TILE_CACHE_DIR = "tile_cache"
    }
}
