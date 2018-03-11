package de.westnordost.streetcomplete.tangram;

import com.mapzen.tangram.HttpHandler;

import java.io.File;
import okhttp3.Callback;

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
		return super.onRequest(url, cb);
	}

	public void onCancel(String url) {
		if(url != null) url += "?api_key=" + apiKey;
		super.onCancel(url);
	}

}
