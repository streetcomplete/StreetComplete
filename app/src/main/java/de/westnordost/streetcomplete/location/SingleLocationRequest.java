package de.westnordost.streetcomplete.location;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

/** Request ONE location update */
public class SingleLocationRequest implements LocationListener, LostApiClient.ConnectionCallbacks
{
	private final LostApiClient lostApiClient;

	private int priority;
	private Callback listener;

	public interface Callback
	{
		void onLocation(Location location);
	}

	public SingleLocationRequest(Context context)
	{
		lostApiClient = new LostApiClient.Builder(context).addConnectionCallbacks(this).build();

	}

	/** @param priority use one of the LocationRequest.PRIORITY_* constants */
	public void startRequest(int priority, Callback listener)
	{
		this.priority = priority;
		this.listener = listener;
		if(!lostApiClient.isConnected()) lostApiClient.connect();
	}

	public void stopRequest()
	{
		try // TODO remove when https://github.com/mapzen/lost/issues/143 is solved
		{
			LocationServices.FusedLocationApi.removeLocationUpdates(lostApiClient, this);
			lostApiClient.disconnect();
		} catch(NullPointerException e) {
			e.printStackTrace();
		}
	}

	@Override public void onConnected() throws SecurityException
	{
		LocationRequest r = LocationRequest.create().setPriority(priority);
		LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, r, this);
	}

	@Override public void onLocationChanged(Location location)
	{
		listener.onLocation(location);
		stopRequest();
	}

	@Override public void onProviderDisabled(String provider) {}
	@Override public void onProviderEnabled(String provider) {}
	@Override public void onConnectionSuspended() {}
}