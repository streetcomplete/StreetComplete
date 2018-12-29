package de.westnordost.streetcomplete.tangram;

import android.support.annotation.NonNull;

import com.mapzen.tangram.HttpHandler;

import java.io.File;
import java.io.IOException;

import de.westnordost.streetcomplete.ApplicationConstants;
import okhttp3.CacheControl;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.internal.Version;

public class TileHttpHandler extends HttpHandler
{
	private final String apiKey;

	public TileHttpHandler(String apiKey)
	{
		super();
		this.apiKey = apiKey;
	}

	public TileHttpHandler(String apiKey, File directory, long maxSize)
	{
		super(directory, maxSize);
		this.apiKey = apiKey;
	}

	@Override public void onRequest(@NonNull String url, @NonNull Callback cb, long requestHandle)
	{
		HttpUrl httpUrl = HttpUrl.parse(url + "?api_key=" + apiKey);
		if (httpUrl == null) {
			cb.onFailure(null, new IOException("HttpUrl failed to parse url=" + url));
		}
		else {
			Request.Builder builder = new Request.Builder()
				.url(httpUrl)
				.tag(requestHandle)
				.header("User-Agent", ApplicationConstants.USER_AGENT + " / " + Version.userAgent());

			CacheControl cacheControl = cachePolicy.apply(httpUrl);
			if (cacheControl != null) {
				builder.cacheControl(cacheControl);
			}
			Request request = builder.build();
			okClient.newCall(request).enqueue(cb);
		}
	}
}
