package de.westnordost.streetcomplete.tangram;

import com.mapzen.tangram.HttpHandler;

import java.io.File;

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

	public boolean onRequest(String url, Callback cb) {
		if(url != null) url += "?api_key=" + apiKey;
		HttpUrl httpUrl = HttpUrl.parse(url);
		Request.Builder builder = new Request.Builder().url(httpUrl).header("User-Agent", ApplicationConstants.USER_AGENT + " / " + Version.userAgent());
		CacheControl cacheControl = cachePolicy.apply(httpUrl);
		if (cacheControl != null) {
			builder.cacheControl(cacheControl);
		}
		Request request = builder.build();
		okClient.newCall(request).enqueue(cb);
		return true;
	}

	public void onCancel(String url) {
		if(url != null) url += "?api_key=" + apiKey;
		super.onCancel(url);
	}

}
