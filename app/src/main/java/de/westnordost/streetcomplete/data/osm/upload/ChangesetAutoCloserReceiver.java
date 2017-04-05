package de.westnordost.streetcomplete.data.osm.upload;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class ChangesetAutoCloserReceiver extends WakefulBroadcastReceiver
{
	@Override public void onReceive(Context context, Intent intent)
	{
		Intent service = new Intent(context, ChangesetAutoCloserService.class);
		startWakefulService(context, service);
	}
}
