package de.westnordost.streetcomplete.util;

import android.view.View;
import android.view.ViewTreeObserver;

public class ViewUtils
{
	public static void postOnLayout(View view, final Runnable runnable)
	{
		if(view != null)
		{
			ViewTreeObserver vto = view.getViewTreeObserver();
			if (vto.isAlive())
			{
				vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
				{
					@Override public void onGlobalLayout()
					{
						view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
						runnable.run();
					}
				});
			}
		}
	}
}
