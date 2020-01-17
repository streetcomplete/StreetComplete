package de.westnordost.streetcomplete.map

import com.mapzen.tangram.networking.DefaultHttpHandler
import de.westnordost.streetcomplete.ApplicationConstants
import okhttp3.*
import okhttp3.internal.Version
import java.io.File
import java.util.concurrent.TimeUnit

class CachingHttpHandler @JvmOverloads constructor(
    private val apiKey: String?,
    private val cacheDirectory: File?,
    private val maxCacheSizeInBytes: Long = 16 * 1024 * 1024,
    private val cacheControl: CacheControl = CacheControl.Builder().maxStale(7, TimeUnit.DAYS).build()
) : DefaultHttpHandler() {

    override fun configureClient(builder: OkHttpClient.Builder) {
        if (cacheDirectory?.exists() == true) {
            builder.cache(Cache(cacheDirectory, maxCacheSizeInBytes))
        }
    }

    override fun configureRequest(url: HttpUrl, builder: Request.Builder) {
        if (apiKey != null)
            builder.url(url.newBuilder().addQueryParameter("api_key", apiKey).build())

        builder
            .cacheControl(cacheControl)
            .header("User-Agent", ApplicationConstants.USER_AGENT + " / " + Version.userAgent())
    }
}
