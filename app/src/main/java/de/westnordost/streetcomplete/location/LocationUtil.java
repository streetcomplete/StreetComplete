package de.westnordost.streetcomplete.location;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

public class LocationUtil
{
	public static boolean isLocationSettingsOn(Context context)
	{
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

	public static boolean isNewLocationApi()
	{
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}

	// because LocationManager.MODE_CHANGED is not defined before KitKat
	public static String MODE_CHANGED = "android.location.MODE_CHANGED";
}
