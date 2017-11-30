package de.westnordost.streetcomplete.location;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import static android.location.LocationManager.PROVIDERS_CHANGED_ACTION;

public class LocationUtil
{
	public static boolean isLocationOn(Context context)
	{
		if(!hasLocationPermission(context)) return false;

		String locationProviders;
		try
		{
			if (isNewLocationApi())
			{
				int locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
				return locationMode != Settings.Secure.LOCATION_MODE_OFF;
			}
			else
			{
				locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
				return !TextUtils.isEmpty(locationProviders);
			}
		}
		catch(Settings.SettingNotFoundException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static boolean hasLocationPermission(Context context)
	{
		return ContextCompat.checkSelfPermission(context,
				Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
	}

	public static IntentFilter createLocationAvailabilityIntentFilter()
	{
		String action = LocationUtil.isNewLocationApi() ? MODE_CHANGED : PROVIDERS_CHANGED_ACTION;
		return new IntentFilter(action);
	}

	// because LocationManager.MODE_CHANGED is not defined before KitKat
	private static final String MODE_CHANGED = "android.location.MODE_CHANGED";

	private static boolean isNewLocationApi()
	{
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}
}
