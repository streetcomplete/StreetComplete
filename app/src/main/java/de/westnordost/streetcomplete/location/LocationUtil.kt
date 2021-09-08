package de.westnordost.streetcomplete.location;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;

public class LocationUtil
{
	public static boolean isLocationEnabled(@NonNull Context context)
	{
		return hasLocationPermission(context) && LocationManagerCompat
				.isLocationEnabled(ContextCompat.getSystemService(context, LocationManager.class));
	}

	public static boolean hasLocationPermission(@NonNull Context context)
	{
		return ContextCompat.checkSelfPermission(context,
				Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
	}

	public static IntentFilter createLocationAvailabilityIntentFilter()
	{
		return new IntentFilter(LocationManager.MODE_CHANGED_ACTION);
	}
}
